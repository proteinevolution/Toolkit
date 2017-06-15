package controllers

import java.io.{FileInputStream, ObjectInputStream}
import javax.inject.{Inject, Named, Singleton}

import actors.JobActor.{Delete, PrepareJob, StartJob}
import actors.JobIDActor
import akka.actor.ActorRef
import models.Constants
import models.database.jobs._
import models.database.users.User
import models.job.JobActorAccess
import models.search.JobDAO
import modules.LocationProvider
import org.joda.time.DateTime
import play.api.cache._
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import better.files._
import models.tools.ToolFactory
import modules.db.MongoStore
import modules.tel.env.Env
import play.Logger
import play.modules.reactivemongo.ReactiveMongoApi

/**
  * Created by lzimmermann on 02.12.16.
  */
@Singleton
final class JobController @Inject()(jobActorAccess: JobActorAccess,
                                    val reactiveMongoApi: ReactiveMongoApi,
                                    @Named("jobIDActor") jobIDActor: ActorRef,
                                    userSessions : UserSessions,
                                    mongoStore : MongoStore,
                                    env: Env,
                                    @NamedCache("userCache") implicit val userCache: CacheApi,
                                    implicit val locationProvider: LocationProvider,
                                    val jobDao: JobDAO,
                                    val toolFactory: ToolFactory)
    extends Controller
    with Constants
    with Common {

  /**
    *  Loads one minified version of a job to the view, given the jobID
    *
    */
  def loadJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      // Find the Job in the database
      mongoStore.selectJob(jobID).map {
        case Some(job) =>
          // Check if the Job was deleted or not
          job.deletion match {
            case Some(_) =>
              NotFound
            case None =>
              // Check if the user is the Owner or if the job is public
              //if (job.ownerID.contains(user.userID) || job.ownerID.isEmpty)
              Ok(job.cleaned())
            //else NotFound
          }
        case None => NotFound
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
      jobActorAccess.sendToJobActor(jobID, StartJob(jobID))
      Ok(Json.toJson(Json.obj("message" -> "Starting Job...")))
    }
  }

  def submitJob(toolName: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      // Grab the formData from the request data
      request.body.asMultipartFormData match {
        case Some(mpfd) =>
          var formData = mpfd.dataParts.mapValues(_.mkString(formMultiValueSeparator))
          mpfd.file("file").foreach { file =>
            var source = scala.io.Source.fromFile(file.ref.file)
            formData = try { formData.updated("alignment", source.getLines().mkString("\n")) } finally {
              source.close()
            }
          }
          // Determine the jobID
          (formData.get("jobID") match {
            case Some(jobID) =>
              // Bad Request if the jobID can be matched to one from the database
              mongoStore.selectJob(jobID).map { job =>
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
              var params: Map[String, String] = formData
              formData.filterKeys(parameter => toolParams.contains(parameter)).map { paramWithValue =>
                paramWithValue._1 -> toolParams(paramWithValue._1).paramType.validate(paramWithValue._2)
              }
              params = params.updated("regkey", modellerKey)
              // get checkbox value
              // TODO: mailUpdate some how gets lost in the filter function above
              val emailUpdate = formData.get("emailUpdate") match {
                case Some(x) => true
                case _       => false
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
                emailUpdate = emailUpdate,
                tool = toolName,
                toolnameLong = None,
                label = params.get("label"),
                watchList = List(user.userID),
                dateCreated = Some(jobCreationTime),
                dateUpdated = Some(jobCreationTime),
                dateViewed = Some(jobCreationTime)
              )

              // TODO may want to use a different way to identify our users - use the account type in the user perhaps?
              val isFromInstitute = user.getUserData.eMail.matches(".+@tuebingen.mpg.de")

              // Add Job to user in database
              userSessions.modifyUserWithCache(BSONDocument(User.IDDB   -> user.userID),
                                  BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> job.jobID)))

              // Add job to database
              mongoStore.insertJob(job).map {
                case Some(_) =>
                  // Send the job to the jobActor for preparation
                  jobActorAccess.sendToJobActor(jobID, PrepareJob(job, params, startJob = false, isFromInstitute))
                  // Notify user that the job has been submitted
                  Ok(Json.obj("successful" -> true, "jobID" -> jobID))
                    .withSession(userSessions.sessionCookie(request, user.sessionID.get, Some(user.getUserData.nameLogin)))
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

  /**
    * Sends a deletion request to the job actor.
    * @return
    */
  def delete(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    Logger.info("Delete Action in JobController reached")
    userSessions.getUser.map { user =>
      jobActorAccess.sendToJobActor(jobID, Delete(jobID, user.userID))
      Ok
    }
  }

  /**
    * TODO implement me
    * @param jobID
    * @return
    */
  def checkHash(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).flatMap {
        case Some(job) =>
          val params: Map[String, String] = {
            val ois = new ObjectInputStream(new FileInputStream((jobPath / jobID / serializedParam).pathAsString))
            val x   = ois.readObject().asInstanceOf[Map[String, String]]
            ois.close()
            x
          }
          val jobHash = JobHash.generateJobHash(job, params, env, jobDao)
          // Match the hash
          jobDao.matchHash(jobHash).flatMap { richSearchResponse =>
            // Generate a list of hits and convert them into a list of future option jobs
            val mainIDs = richSearchResponse.getHits.getHits.toList.map { hit =>
              BSONObjectID.parse(hit.getId).getOrElse(BSONObjectID.generate())
            }

            Logger.info(mainIDs.map(_.stringify).mkString(", "))
            // Find the Jobs in the Database
            mongoStore.findJobs(BSONDocument(Job.IDDB -> BSONDocument("$in" -> mainIDs))).map { jobList =>
              val foundMainIDs   = jobList.map(_.mainID)
              val unFoundMainIDs = mainIDs.filterNot(checkMainID => foundMainIDs contains checkMainID)
              val jobsFiltered   = jobList.filter(_.status == Done)

              // Delete index-zombie jobs
              unFoundMainIDs.foreach { mainID =>
                Logger.info("[WARNING]: job in index but not in database: " + mainID.stringify)
                jobDao.deleteJob(mainID.stringify)
              }
              jobsFiltered.lastOption match {
                case Some(oldJob) =>
                  Ok(Json.toJson(Json.obj("jobID" -> oldJob.jobID, "dateCreated" -> oldJob.dateCreated)))
                case None => NotFound
              }
            }
          }
      }
    }
  }
}
