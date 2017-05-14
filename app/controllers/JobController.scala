package controllers

import java.io.{FileInputStream, ObjectInputStream}
import javax.inject.{Inject, Named, Singleton}

import actors.JobActor.{CreateJob, Delete, PrepareJob, StartJob}
import actors.JobIDActor
import akka.actor.ActorRef
import models.Constants
import models.database.jobs._
import models.database.users.User
import models.job.JobActorAccess
import models.search.JobDAO
import modules.{CommonModule, LocationProvider}
import org.joda.time.DateTime
import play.api.cache._
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import better.files._
import models.tools.ToolFactory
import modules.tel.env.Env
import play.Logger

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

  def startJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>
      jobActorAccess.sendToJobActor(jobID, StartJob(jobID))
      Ok(Json.toJson(Json.obj("message" -> "Starting Job...")))
    }
  }

  def submitJob(toolName: String): Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      // Grab the formData from the request data
      request.body.asMultipartFormData match {
        case Some(mpfd) =>
          val formData = mpfd.dataParts.mapValues(_.mkString(formMultiValueSeparator))
          // Determine the jobID
          (formData.get("jobID") match {
            case Some(jobID) =>
              // Bad Request if the jobID can be matched to one from the database
              selectJob(jobID).map { job =>
                if (job.isDefined) None else Some(jobID)
              }
            case None =>
              // Use jobID Actor to get a new random jobID
              Future.successful(Some(JobIDActor.provide))
          }).flatMap {
            case Some(jobID) =>
              // Load the parameters for the tool
              val toolParams = toolFactory.values(toolName).params

              // Filter invalid parameters
              val params =
                formData.filterKeys(parameter => toolParams.contains(parameter)).map { paramWithValue =>
                  paramWithValue._1 -> toolParams(paramWithValue._1).paramType.validate(paramWithValue._2)
                }

              // Set job as either private or public
              val ownerOption = if (params.get("public").isEmpty) { Some(user.userID) } else { None }

              // Get the current date to set it for all three dates
              val jobCreationTime = DateTime.now()

              // Create a new Job object for the job and set the initial values
              val job = Job(
                mainID = BSONObjectID.generate(),
                jobID = jobID,
                ownerID = ownerOption,
                status = Submitted,
                emailUpdate = params.get(Job.EMAILUPDATE).isDefined,
                tool = toolName,
                label = params.get("label").flatten,
                watchList = List(user.userID),
                dateCreated = Some(jobCreationTime),
                dateUpdated = Some(jobCreationTime),
                dateViewed = Some(jobCreationTime)
              )

              // TODO may want to use a different way to identify our users - use the account type in the user perhaps?
              val isFromInstitute = user.getUserData.eMail.matches(".+@tuebingen.mpg.de")

              // Add Job to user in database
              modifyUserWithCache(BSONDocument(User.IDDB   -> user.userID),
                                  BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> job.jobID)))

              // Add job to database
              insertJob(job).map {
                case Some(_) =>
                  // Send the job to the jobActor for preparation
                  jobActorAccess.sendToJobActor(jobID, PrepareJob(job, formData, startJob = false, isFromInstitute))
                  // Notify user that the job has been submitted
                  Ok(Json.obj("successful" -> true, "jobID" -> jobID))
                    .withSession(sessionCookie(request, user.sessionID.get, Some(user.getUserData.nameLogin)))
                case None =>
                  // Something went wrong when pushing to the DB
                  Ok(Json.obj("successful" -> false, "message" -> "Could not write to DB."))
              }
            case None =>
              // TODO Should not be a Bad Request but a message stating that the job ID is already taken.
              Future.successful(Ok(Json.obj("successful" -> false, "message" -> "Job ID is already taken.")))
          }
        case None =>
          // No form data - something went wrong.
          Future.successful(Ok(Json.obj("successful" -> false, "message" -> "The form was invalid.")))
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


  /**
    * TODO implement me
    * @param jobID
    * @return
    */
  def checkHash(jobID : String) : Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      findJob(BSONDocument(Job.JOBID -> jobID)).flatMap {
        case Some(job) =>
          val params: Map[String, String] = {
            val ois = new ObjectInputStream(new FileInputStream((jobPath/jobID/serializedParam).pathAsString))
            val x   = ois.readObject().asInstanceOf[Map[String, String]]
            ois.close()
            x
          }
          val jobHash = JobHash.generateJobHash(job, params, env, jobDao)
          Logger.info(jobHash.inputHash)

          // Match the hash
          jobDao.matchHash(jobHash).flatMap { richSearchResponse =>
            Logger.info("Retrieved richSearchResponse")
            Logger.info("success: " + richSearchResponse.getHits.getHits.map(_.getId).mkString(", "))
            Logger.info("hits: " + richSearchResponse.totalHits)

            // Generate a list of hits and convert them into a list of future option jobs
            val mainIDs = richSearchResponse.getHits.getHits.toList.map { hit =>
              BSONObjectID.parse(hit.getId).getOrElse(BSONObjectID.generate())
            }

            // Find the Jobs in the Database
            findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs))).map { jobList =>
              val foundMainIDs = jobList.map(_.mainID)
              val unFoundMainIDs = mainIDs.filterNot(checkMainID => foundMainIDs contains checkMainID)
              val jobsPartition = jobList.filter(_.status == Done)

              // Delete index-zombie jobs
              unFoundMainIDs.foreach { mainID =>
                Logger.info("[WARNING]: job in index but not in database: " + mainID.stringify)
                jobDao.deleteJob(mainID.stringify)
              }
              Ok(Json.toJson(Json.obj("jobID" -> jobsPartition.lastOption.map(_.jobID))))
            }
          }
      }
    }
  }
}
