package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.JobActor.{CreateJob, Delete}
import actors.JobIDActor
import akka.actor.ActorRef
import models.Constants
import models.database.jobs._
import models.database.users.User
import models.job.JobActorAccess
import models.search.JobDAO
import modules.{CommonModule, LocationProvider}
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import better.files._
import models.tools.ToolFactory
import modules.tel.env.Env

/**
  * Created by lzimmermann on 02.12.16.
  */
@Singleton
final class JobController @Inject()(jobActorAccess: JobActorAccess,
                                    @Named("jobIDActor") jobIDActor: ActorRef,
                                    env: Env,
                                    @NamedCache("userCache") implicit val userCache: CacheApi,
                                    implicit val locationProvider: LocationProvider,
                                    val jobDao: JobDAO,
                                    val toolFactory: ToolFactory,
                                    val reactiveMongoApi: ReactiveMongoApi)
    extends Controller
    with UserSessions
    with CommonModule
    with Constants {

  /**
    *  Loads one minified version of a job to the view, given the jobID
    *
    */
  def loadJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    // Find the Job in the database
    selectJob(jobID).map {
      case Some(job) =>
        // Check if the Job was deleted or not
        job.deletion match {
          case Some(deletionReason) =>
            NotFound
          case None =>
            Ok(job.cleaned())
        }
      case None => NotFound
    }
  }
  def listJobs: Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      findJobs(BSONDocument(Job.JOBID -> BSONDocument("$in" -> user.jobs))).map { jobs =>
        Ok(Json.toJson(jobs.map(_.cleaned())))
      }
    }
  }

  def check(toolname: String, jobID: Option[String], hash: Boolean): Action[AnyContent] = Action.async {
    implicit request =>
      getUser.flatMap { user =>
        // Determine the jobID
        (jobID match {

          // Bad Request if the jobID can be matched to one from the database
          case Some(id) =>
            selectJob(id).map { job =>
              if (job.isDefined) Left(BadRequest) else Right(id)
            }

          case None =>
            Future.successful(Right(JobIDActor.provide))

        }).flatMap {

          case Left(status)    => Future.successful(status)
          case Right(jobIDnew) =>
            // Grab the formData from the request data
            val formData: Map[String, String] =
              request.body.asMultipartFormData.get.dataParts.mapValues(_.mkString(formMultiValueSeparator))

            // Get the parameters of the tool for validation purpose
            val toolParams = toolFactory.values(toolname).params

            // validate formData if it is a tool parameter
            val x = formData.filterKeys(x => toolParams.contains(x)).map { t =>
              t._1 -> toolParams(t._1).paramType.validate(t._2)
            }

            //

            // If we do not hash (usually forwarding) just provide the new JobID
            if (!hash) {
              Future.successful(
                Ok(Json.obj("jobSubmitted" -> true, "jobID" -> jobIDnew))
                  .withSession(sessionCookie(request, user.sessionID.get, Some(user.getUserData.nameLogin))))

            } else {

              // get hold of the database in use

              case class DB(name: Option[String], mTime: Option[String])

              val HHBLITSDBMTIME  = env.get("HHBLITS").toFile.lastModifiedTime.toString
              val HHSUITEDBMTIME  = env.get("HHSUITE").toFile.lastModifiedTime.toString
              val STANDARDDB      = env.get("STANDARD") + "/" + formData.getOrElse("standarddb", "")
              val STANDARDDBMTIME = STANDARDDB.toFile.lastModifiedTime.toString

              val jobDB = formData match {

                case x if x isDefinedAt "standarddb" => DB(Some(STANDARDDB), Some(STANDARDDBMTIME))
                case x if x isDefinedAt "hhblitsdb" =>
                  DB(Some(formData.getOrElse("hhblitsdb", "")), Some(HHBLITSDBMTIME))
                case x if x isDefinedAt "hhsuitedb" =>
                  DB(Some(formData.getOrElse("hhsuitedb", "")), Some(HHSUITEDBMTIME))
                case _ => DB(None, Some("1970-01-01T00:00:00Z"))

              }

              val inputHash = jobDao.generateHash(formData).toString()
              val rsHash    = jobDao.generateRSHash(toolname)
              val toolHash  = jobDao.generateToolHash(toolname)

              println("Runscript hash generated: " + rsHash)
              println("Tool hash generated: " + toolHash)
              println("Job hash generated: " + inputHash)

              Logger.info("Try to match Hash")
              jobDao.matchHash(inputHash, rsHash, jobDB.name, jobDB.mTime, toolname, toolHash).flatMap {
                richSearchResponse =>
                  Logger.info("Retrieved richSearchResponse")
                  println("success: " + richSearchResponse)
                  println("hits: " + richSearchResponse.totalHits)

                  // Generate a list of hits and convert them into a list of future option jobs
                  val mainIDs = richSearchResponse.getHits.getHits.toList.map { hit =>
                    BSONObjectID
                      .parse(hit.getId)
                      .getOrElse(BSONObjectID.generate()) // Not optimal, as a fake Object ID is generated, but apply(id : String) was deprecated
                  }

                  // Find the Jobs in the Database
                  findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs))).map { jobList =>
                    val foundMainIDs   = jobList.map(_.mainID)
                    val unFoundMainIDs = mainIDs.filterNot(checkMainID => foundMainIDs contains checkMainID)
                    val jobsPartition  = jobList.partition(_.status != Done)

                    // Delete index-zombie jobs
                    unFoundMainIDs.foreach { mainID =>
                      println("[WARNING]: job in index but not in database: " + mainID.stringify)
                      jobDao.deleteJob(mainID.stringify)
                    }

                    jobsPartition._2.lastOption match {
                      case Some(job) =>
                        Logger.info("Returning response")

                        Ok(
                          Json.obj("jobSubmitted" -> true,
                                   "jobStarted"   -> false,
                                   "existingJobs" -> true,
                                   "existingJob"  -> job.cleaned(),
                                   "jobID"        -> jobIDnew))
                          .withSession(sessionCookie(request, user.sessionID.get, Some(user.getUserData.nameLogin)))

                      case None =>
                        Logger.info("Returning response")

                        Ok(
                          Json.obj("jobSubmitted"  -> true,
                                   "identicalJobs" -> false,
                                   "existingJobs"  -> false,
                                   "jobID"         -> jobIDnew))
                          .withSession(sessionCookie(request, user.sessionID.get, Some(user.getUserData.nameLogin)))
                    }
                  }
              }
            }
        }
      }
  }

  def create(toolname: String, jobID: String): Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      Logger.info("Creating a new Job. User:\n" + user.toString)
      selectJob(jobID).map {
        case Some(_) => BadRequest //If the jobID has become unavailable between checking and submitting
        case None =>
          request.body.asMultipartFormData match {
            case Some(mpfd) =>
              var formData = mpfd.dataParts.mapValues(_.mkString(formMultiValueSeparator))
              mpfd.file("file").foreach { file =>
                println(scala.io.Source.fromFile(file.ref.file).getLines().mkString("\n"))
                formData =
                  formData.updated("alignment", scala.io.Source.fromFile(file.ref.file).getLines().mkString("\n"))
              }
              jobActorAccess.sendToJobActor(jobID, CreateJob(jobID, user, toolname, formData))
              Ok
            case None =>
              BadRequest // Job has no proper form
          }
      }
    }
  }

  /**
    * Sends a deletion request to the job actor.
    * @return
    */
  def delete(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    Logger.info("Delete Action in JobController reached")
    getUser.map { user =>
      jobActorAccess.sendToJobActor(jobID, Delete(jobID, user.userID))
      Ok
    }
  }
}
