package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobActor
import actors.JobActor.{Delete, RunscriptData}
import actors.Master.{CreateJob, JobMessage}
import akka.actor.{ActorRef, ActorSystem}
import models.Values
import models.database.{User, JobDeletionFlag, JobDeletion, Job}
import models.job.JobIDProvider
import models.search.JobDAO
import modules.{CommonModule, LocationProvider}
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import better.files._
import modules.tel.TEL

/**
  * Created by lzimmermann on 02.12.16.
  */
@Singleton
final class JobController @Inject() (jobIDProvider                                    : JobIDProvider,
                                     actorSystem                                      : ActorSystem,
                                     jobActorFactory                                  : JobActor.Factory,
                                     implicit val userCache                           : CacheApi,
                                     val values                                       : Values,
                                     implicit  val locationProvider                   : LocationProvider,
                                     @Named("master") master                          : ActorRef,
                                     val jobDao                                       : JobDAO,
                                     val tel                                          : TEL,
                                     @NamedCache("jobitem") jobitemCache              : CacheApi,
                                     @NamedCache("jobActorCache") val jobActorCache   : CacheApi,
                                     val reactiveMongoApi                             : ReactiveMongoApi)
                                     extends Controller with UserSessions with CommonModule {

  /**
    *  Loads one minified version of a job to the view, given the jobID
    *
    */
  def loadJob(jobID : String) : Action[AnyContent] = Action.async { implicit request =>

        // TODO Load job has to notify master, that user again belongs to the watchlist
        // TODO Ensure that deleted Jobs cannot be loaded
        selectJob(jobID).map {
          case Some(job) => Ok(job.cleaned())
          case None => NotFound
        }
  }


  def listJobs : Action[AnyContent] = Action.async { implicit request =>

    getUser.flatMap { user =>
        findJobs(BSONDocument(Job.JOBID -> BSONDocument("$in" -> user.jobs))).map { jobs =>
          Ok(Json.toJson( jobs.map(_.cleaned())))
        }
    }
  }


  def check(toolname: String, jobID: Option[String]) : Action[AnyContent] = Action.async { implicit request =>

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


          val formData = request.body.asMultipartFormData.get.dataParts.mapValues(_.mkString)
          val DB = formData.getOrElse("standarddb","").toFile  // get hold of the database in use
          val inputHash = jobDao.generateHash(formData).toString()
          val rsHash = jobDao.generateRSHash(toolname)
          val toolHash = jobDao.generateToolHash(toolMap(toolname).toolNameLong)
          println("Runscript hash generated: " + rsHash)
          println("Tool hash generated: " + toolHash)
          println("Job hash generated: " + inputHash)
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
          Logger.info("Try to match Hash")
          jobDao.matchHash(inputHash, rsHash, dbName, dbMtime, toolname, toolHash).flatMap { richSearchResponse =>

            Logger.info("Retrieved richSearchResponse")
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
                    "existingJob"   ->job.cleaned(),
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


  def create(toolname: String, jobID: String) : Action[AnyContent] =  Action.async { implicit request =>

    // Just grab the formData and send to Master
    getUser.flatMap { user =>

      selectJob(jobID).map {

        case Some(_) => BadRequest

        case None =>
          val formData = request.body.asMultipartFormData.get.dataParts.mapValues(_.mkString)
          master ! CreateJob(jobID, (user, None), RunscriptData(toolname, formData))
          Ok
      }
    }
  }



  /**
    * Marks a Job for deletion
    * TODO introduce jobActor Cache
    *
    * @return
    */
  def delete(jobID : String) =  Action.async { implicit request =>
    Logger.info("Delete Action in JobController reached")
    getUser.flatMap { user =>
      findJob(BSONDocument(Job.JOBID -> jobID)).map {
        case Some(job) =>
          Logger.info("Found Jobs for deletion: " + job.jobID)
          // Check if the User owns the job
          if (job.ownerID.contains(user.userID)) {
            // Tell the master to delete the job
            master ! JobMessage(jobID, Delete)
            // Mark the job in mongoDB
            updateJobs(BSONDocument(Job.IDDB      -> job.mainID),
                       BSONDocument("$set"        ->
                       BSONDocument(Job.DELETION  -> JobDeletion(JobDeletionFlag.OwnerRequest, Some(DateTime.now()))),
                       BSONDocument("$unset"      ->
                       BSONDocument(Job.WATCHLIST -> ""))))
          } else {
            // Mark public job as deleteable
            /*
            if(job.ownerID.isEmpty) {
              updateJobs(BSONDocument(Job.IDDB -> job.mainID),
                         BSONDocument("$set" ->
                         BSONDocument(Job.DELETION -> JobDeletion(JobDeletionFlag.PublicRequest, Some(DateTime.now())))))
            }*/
            // Clear job which is not owned by the User
            updateJobs(BSONDocument(Job.IDDB -> job.mainID),
                       BSONDocument("$pull"  ->
                       BSONDocument(Job.WATCHLIST -> user.userID)))
          }
          Ok
        case None =>
          NotFound
      }
    }
  }

  /**
    * Marks multiple Jobs for deletion
    * TODO introduce jobActor Cache
    *
    * @return
    */
  def deleteMulti() =  Action.async { implicit request =>
    Logger.info("DeleteMulti Action in JobController reached")
    getUser.map { user =>
      // evaluate all jobIDs from the ids list
      request.getQueryString("ids").map { str =>
        val jobIDs = str.split(",").toList
        jobIDs.foreach{ jobID =>
          master ! JobMessage(jobID, Delete)
        }
        findJobs(BSONDocument(Job.JOBID -> BSONDocument("$in" -> jobIDs))).map { jobs =>
          Logger.info("Found Jobs: " + jobs)
          // filter jobs which are not owned by the user
          val ownershipPatition = jobs.partition(_.ownerID.contains(user.userID))
          // Remove owned jobs
          val ownedJobs = ownershipPatition._1.map(_.mainID)
          Logger.info("Found jobs Owned by the User: " + ownedJobs)
          updateJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" ->ownedJobs)),
                     BSONDocument("$set"   ->
                     BSONDocument(Job.DELETION  -> JobDeletion(JobDeletionFlag.OwnerRequest, Some(DateTime.now()))),
                     BSONDocument("$unset" ->
                     BSONDocument(Job.WATCHLIST -> ""))))
          // Mark public jobs as deleteable
          /*
          val publicJobs = ownershipPatition._2.filter(_.ownerID.isEmpty).map(_.mainID)
          Logger.info("Found public jobs: " + publicJobs)
          updateJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> publicJobs)),
                     BSONDocument("$set"   ->
                     BSONDocument(Job.DELETION -> JobDeletion(JobDeletionFlag.PublicRequest, Some(DateTime.now())))))
          */
          // Clear jobs which are not owned by the User
          val otherJobs =  ownershipPatition._2.map(_.mainID)
          Logger.info("Found jobs which should only be cleared: " + otherJobs)
          updateJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> otherJobs)),
                     BSONDocument("$pull"  ->
                     BSONDocument(Job.WATCHLIST -> user.userID)))
        }
      }
      Ok("started")
    }
  }
}
