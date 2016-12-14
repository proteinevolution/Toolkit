package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobActor
import actors.JobActor.RunscriptData
import actors.Master.CreateJob
import actors.UserManager.AddJobWatchList
import akka.actor.{ActorRef, ActorSystem}
import models.Values
import models.database.{Job, Jobitem, User}
import models.job.JobIDProvider
import models.search.JobDAO
import modules.LocationProvider
import modules.tools.FNV
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import better.files._

import scala.util.Success


/**
  * Created by lzimmermann on 02.12.16.
  */
@Singleton
class JobController @Inject() (jobIDProvider: JobIDProvider,
                               actorSystem : ActorSystem,
                               jobActorFactory : JobActor.Factory,
                               implicit val userCache : CacheApi,
                               final val values : Values,
                               implicit  val locationProvider: LocationProvider,
                               @Named("master") master: ActorRef,
                               val jobDao           : JobDAO,
                               @NamedCache("jobitem") jobitemCache : CacheApi,
                               @NamedCache("jobActorCache") val jobActorCache: CacheApi,
                               val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with UserSessions {



  /**
    *  Loads one minified version of a job to the view, given the jobID
    *
    */
  def loadJob(jobID : String) = Action.async { implicit request =>

      Logger.info("Trying to load job")

        selectJob(jobID).map {
          case Some(job) => Ok(job.cleaned())
          case None => NotFound
        }
  }



  def listJobs = Action.async { implicit request =>

    getUser.flatMap { user =>

        Logger.info("User is associated with " + user.jobs.size + " jobs")

        findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> user.jobs))).map { jobs =>

          Logger.info("We have found " + jobs.size + " jobs")

          Ok(Json.toJson( jobs.map(_.cleaned)))
        }
    }
  }


  def check(toolname: String, jobID: Option[String]) = Action.async { implicit request =>

    Logger.info("Check controller reached")

    getUser.flatMap { user =>

      // Determine the jobID
      (jobID match {

        case Some(id) =>
          selectJob(id).map { job => if (job.isDefined) Left(BadRequest) else Right(id) }
        case None =>

          jobIDProvider.provide.map { s =>

            Right(s)
          }

      }).flatMap {

        case Left(status) => Future.successful(status)
        case Right(jobIDnew) =>

          Logger.info("New JobID will be " + jobIDnew)

          Logger.info("Try to obtain formData")
          val formData = request.body.asMultipartFormData.get.dataParts.mapValues(_.mkString)

          Logger.info("Try to make database file")
          val DB = formData.getOrElse("standarddb","").toFile  // get hold of the database in use

          Logger.info("Try to generate hash")
          val inputHash = jobDao.generateHash2(toolname, formData)

          Logger.info("Input Hash generated")

          lazy val dbName = {
            formData.get("standarddb") match {
              case None => Some("none")
              case _ => Some(DB.name)
            }
          }
          lazy val dbMtime = {
            formData.get("standarddb") match {
              case None => Some("1970-01-01T00:00:00Z")
              case _ => Some(DB.lastModifiedTime.toString)
            }
          }

          jobDao.matchHash(inputHash, dbName, dbMtime).flatMap { richSearchResponse =>

            println("success: " + richSearchResponse)
            println("hits: " + richSearchResponse.totalHits)

            // Generate a list of hits and convert them into a list of future option jobs
            val mainIDs = richSearchResponse.getHits.getHits.toList.map { hit =>
              BSONObjectID.parse(hit.getId).getOrElse(BSONObjectID.generate()) // Not optimal, as a fake Object ID is generated, but apply(id : String) was deprecated
            }

            // Find the Jobs in the Database
            findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs))).map { jobList =>

              val foundMainIDs   = jobList.map(_.mainID)
              val unFoundMainIDs = mainIDs.filterNot(checkMainID => foundMainIDs contains checkMainID)
              val jobsPartition   = jobList.partition(_.status == models.database.Error)

              // Delete index-zombie jobs
              unFoundMainIDs.foreach { mainID =>
                println("[WARNING]: job in index but not in database: " + mainID.stringify)
                jobDao.deleteJob(mainID.stringify)
              }

              jobsPartition._2.headOption match {
                case Some(job) =>

                  Logger.info("Returning response")

                  Ok(Json.obj("jobSubmitted"  -> true,
                    "jobStarted"    -> false,
                    "existingJobs"  -> true,
                    "existingJob"   -> job.cleaned(),
                    "jobID" -> jobIDnew)
                  ).withSession(sessionCookie(request, user.sessionID.get))

                case None =>

                  Logger.info("Returning response")

                  Ok(Json.obj("jobSubmitted"  -> true,
                    "identicalJobs" -> false,
                    "existingJobs"  -> false,
                    "jobID" -> jobIDnew)
                  ).withSession(sessionCookie(request, user.sessionID.get))
              }
            }
          }
      }
    }
  }


  def create(toolname: String, jobID: String) =  Action.async { implicit request =>

    // Just grab the formData and send to Master
    getUser.map { user =>
      val formData = request.body.asMultipartFormData.get.dataParts.mapValues(_.mkString)
      master ! CreateJob(jobID, (user, None), RunscriptData(toolname, formData))
      Ok
    }
  }



  def delete(jobID : String ) = Action {



    Ok
  }

}
