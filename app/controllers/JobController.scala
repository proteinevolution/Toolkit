package controllers

import java.io.{ FileInputStream, ObjectInputStream }
import java.security.MessageDigest
import java.time.ZonedDateTime
import javax.inject.{ Inject, Named, Singleton }

import actors.JobActor._
import actors.JobIDActor
import akka.actor.ActorRef
import better.files._
import models.database.jobs._
import models.database.users.User
import models.search.JobDAO
import models.tools.ToolFactory
import models.{ Constants, UserSessions }
import modules.LocationProvider
import modules.db.MongoStore
import modules.tel.env.Env
import play.api.Logger
import play.api.cache._
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONDocument
import services.JobActorAccess

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by lzimmermann on 02.12.16.
 */
@Singleton
final class JobController @Inject()(jobActorAccess: JobActorAccess,
                                    val reactiveMongoApi: ReactiveMongoApi,
                                    @Named("jobIDActor") jobIDActor: ActorRef,
                                    userSessions: UserSessions,
                                    mongoStore: MongoStore,
                                    env: Env,
                                    @NamedCache("userCache") implicit val userCache: SyncCacheApi,
                                    implicit val locationProvider: LocationProvider,
                                    val jobDao: JobDAO,
                                    val toolFactory: ToolFactory,
                                    constants: Constants,
                                    cc: ControllerComponents)
    extends AbstractController(cc)
    with Common {

  /**
   *  Loads one minified version of a job to the view, given the jobID
   *
   */
  def loadJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      // Find the Job in the database
      mongoStore.selectJob(jobID).map {
        case Some(job) => Ok(job.cleaned())
        case None      => NotFound
      }
    }
  }
  def listJobs: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      mongoStore.findJobs(BSONDocument(Job.JOBID -> BSONDocument("$in" -> user.jobs))).map { jobs =>
        Ok(Json.toJson(jobs.map(_.cleaned())))
      }
    }
  }

  def startJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      jobActorAccess.sendToJobActor(jobID, CheckIPHash(jobID))
      Ok(Json.toJson(Json.obj("message" -> "Starting Job...")))
    }
  }

  def submitJob(toolName: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      // Grab the formData from the request data
      request.body.asMultipartFormData match {
        case Some(mpfd) =>
          // map the form data as parameters with values
          var formData = mpfd.dataParts.mapValues(_.mkString(constants.formMultiValueSeparator)) - "file"

          // Merge the file into the "alignment" parameter, if it exists
          mpfd.file("file").foreach { file =>
            val source = scala.io.Source.fromFile(file.ref.path.toFile)
            try {
              formData = formData.updated("alignment", source.getLines().mkString("\n"))
            } finally {
              source.close()
            }
          }

          // Determine the jobID
          (formData.get("jobID") match {
            case Some(jobID) =>
              // Match the pattern of the jobID to check if there are no illegal characters
              jobID match {
                case constants.jobIDVersionOptionPattern(mainJobID, version) =>
                  Logger.info(s"[JobController.submitJob] main jobID: $mainJobID version: $version")
                  // Check if the jobID is already used by a different job
                  mongoStore.selectJob(jobID).map { job =>
                    if (job.isDefined) None else Some(jobID)
                  }
                case _ =>
                  // Pattern failed
                  Future.successful(None)
              }
            case None =>
              // Use jobID Actor to get a new random jobID
              Future.successful(Some(JobIDActor.provide))
          }).flatMap {
            case Some(jobID) =>
              // Load the parameters for the tool
              val toolParams = toolFactory.values(toolName).params

              // Filter invalid parameters
              var params: Map[String, String] = formData

              // Quick fix to remove unused parameter from job hashing TODO fix this.
              formData.get("alignment_two").foreach { alignment =>
                if (alignment.isEmpty) params = params - "alignment_two"
              }

              /**
               * TODO Validate here!
               */
              val validatedFormData: Map[String, Option[String]] =
                formData.filterKeys(parameter => toolParams.contains(parameter)).map { paramWithValue =>
                  paramWithValue._1 -> toolParams(paramWithValue._1).paramType.validate(paramWithValue._2)
                }

              // Check if the user has the Modeller Key when the requested tool is Modeller
              if (toolName == toolFactory.Toolnames.MODELLER && user.userConfig.hasMODELLERKey)
                params = params.updated("regkey", constants.modellerKey)

              // get checkbox value for the update per mail option
              val emailUpdate = formData.get("emailUpdate") match {
                case Some(x) => true
                case _       => false
              }

              // Set job as either private or public
              val ownerOption = if (params.get("public").isEmpty) { Some(user.userID) } else { None }

              // Get the current date to set it for all three dates
              val now = ZonedDateTime.now

              // Check the users bonus time for jobs
              val dateDeletion = user.userData.map(_ => now.plusDays(constants.jobDeletion))

              // Create a new Job object for the job and set the initial values
              val job = Job(
                jobID = jobID,
                ownerID = ownerOption,
                emailUpdate = emailUpdate,
                tool = toolName,
                watchList = List(user.userID),
                dateCreated = Some(now),
                dateUpdated = Some(now),
                dateViewed = Some(now),
                dateDeletion = dateDeletion,
                IPHash = Some(MessageDigest.getInstance("MD5").digest(user.sessionData.head.ip.getBytes).mkString)
              )

              // TODO may want to use a different way to identify our users - use the account type in the user perhaps?
              val isFromInstitute = user.getUserData.eMail.matches(".+@tuebingen.mpg.de")

              // Add job to database
              mongoStore.insertJob(job).flatMap {
                case Some(_) =>
                  // Send the job to the jobActor for preparation
                  jobActorAccess.sendToJobActor(jobID, PrepareJob(job, params, startJob = false, isFromInstitute))

                  // Add Job to user in database
                  userSessions
                    .modifyUserWithCache(BSONDocument(User.IDDB   -> user.userID),
                                         BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> job.jobID)))
                    .map { _ =>
                      // Notify user that the job has been submitted
                      Ok(
                        Json.obj("successful" -> true,
                                 "code"       -> 0,
                                 "message"    -> "Submission successful.",
                                 "jobID"      -> jobID)
                      ).withSession(
                          userSessions.sessionCookie(request, user.sessionID.get)
                        )
                    }
                case None =>
                  // Something went wrong when pushing to the DB
                  Future
                    .successful(Ok(Json.obj("successful" -> false, "code" -> 3, "message" -> "Could not write to DB.")))
              }
            case None =>
              // The job ID is already taken
              Future.successful(
                Ok(Json.obj("successful" -> false, "code" -> 2, "message" -> "Job ID is already taken."))
              )
          }
        case None =>
          // No form data - something went wrong.
          Future.successful(Ok(Json.obj("successful" -> false, "code" -> 1, "message" -> "The form was invalid.")))
      }
    }
  }

  /**
   * Sends a deletion request to the job actor.
   *
   * @return
   */
  def delete(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    Logger.info("Delete Action in JobController reached")
    userSessions.getUser.map { user =>
      jobActorAccess.sendToJobActor(jobID, Delete(jobID, Some(user.userID)))
      Ok
    }
  }

  /**
   * Generates the job hash for the given jobID and looks it up in the DB.
   * @param jobID
   * @return
   */
  def checkHash(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).flatMap {
        case Some(job) =>
          // Reload the paramaters of the file
          val params: Map[String, String] = {
            val ois = new ObjectInputStream(
              new FileInputStream((constants.jobPath / jobID / constants.serializedParam).pathAsString)
            )
            val x = ois.readObject().asInstanceOf[Map[String, String]]
            ois.close()
            x
          }
          // Generate the job hash
          val jobHash = jobDao.generateJobHash(job, params, env)
          // Match the hash
          mongoStore
            .findAndSortJobs(
              BSONDocument(Job.HASH        -> jobHash),
              BSONDocument(Job.DATECREATED -> -1)
            )
            .map { jobList =>
              jobList.find(_.status == Done) match {
                case Some(latestOldJob) =>
                  Ok(
                    Json.toJson(
                      Json.obj(
                        "jobID"       -> latestOldJob.jobID,
                        "dateCreated" -> latestOldJob.dateCreated.get.toInstant.toEpochMilli
                      )
                    )
                  )
                case None =>
                  NotFound
              }
            }
      }
    }
  }
}
