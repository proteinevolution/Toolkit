package controllers

import javax.inject.{Inject, Singleton}

import actors.JobActor.{JobStateChanged, UpdateLog}
import models.Constants
import models.database.jobs._
import models.job.JobActorAccess
import modules.{CommonModule, LocationProvider}
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}
import scala.io.Source

import scala.concurrent.ExecutionContext.Implicits.global

/*
TODO
We can introduce auto-coercion of the Job MainID to the BSONObject ID
 */
/**
  * This controller is supposed to handle request coming from the Backend, such as compute
  * nodes from a gridengine.
  *
  */
@Singleton
final class Jobs @Inject()(jobActorAccess: JobActorAccess,
                           @NamedCache("userCache") implicit val userCache: CacheApi,
                           implicit val locationProvider: LocationProvider,
                           val reactiveMongoApi: ReactiveMongoApi)
    extends Controller
    with CommonModule
    with UserSessions
    with Constants {

  def jobStatusDone(jobID: String, key: String) = Action {

    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Done))
      Ok
    } else BadRequest("Permission denied")
  }

  def jobStatusError(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Error))
      Ok
    } else BadRequest("Permission denied")
  }

  def jobStatusRunning(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Running))
      Ok
    } else BadRequest("Permission denied")
  }

  def jobStatusQueued(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Queued))
      Ok
    } else BadRequest("Permission denied")
  }

  def updateLog(jobID: String) = Action {
    jobActorAccess.sendToJobActor(jobID, UpdateLog(jobID))
    Ok
  }

  //TODO make secure
  def SGEID(jobID: String, sgeID: String) = Action {

    findJob(BSONDocument(Job.JOBID -> jobID)).foreach {

      case Some(job) =>
        modifyJob(BSONDocument(Job.JOBID -> job.jobID),
                  BSONDocument("$set"    -> BSONDocument("clusterData.sgeid" -> sgeID)))
        Logger.info(jobID + " gets job-ID " + sgeID + " on SGE")
      case None =>
        Logger.info("Unknown ID " + jobID.toString)
    }

    Ok

  }

  def pushMessage(jobID: String, message: String) = Action {
    //userManager ! RunningJobMessage(reactivemongo.bson.BSONObjectID.parse(jobID).get, message)
    Ok
  }

  // TODO make secure

  def updateDateViewed(jobID: String) = Action {

    modifyJob(BSONDocument(Job.JOBID -> jobID),
              BSONDocument("$set"    -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(DateTime.now().getMillis))))
    Ok
  }

  /**
    *
    * Creates new annotation document and modifies this if it already exists in one method
    *
    *
    * @param jobID
    * @param content
    * @return
    */
  def annotation(jobID: String, content: String): Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      findJob(BSONDocument(Job.JOBID -> jobID)).map {

        case x if x.get.ownerID.get == user.userID =>
          val entry = JobAnnotation(mainID = BSONObjectID.generate(),
                                    jobID = jobID,
                                    content = content,
                                    dateCreated = Some(DateTime.now()))

          upsertAnnotation(entry)

          modifyAnnotation(BSONDocument(JobAnnotation.JOBID -> jobID),
                           BSONDocument("$set"              -> BSONDocument(JobAnnotation.CONTENT -> content)))
          Ok("annotation upserted")

        case _ =>
          Logger.info("Unknown ID " + jobID.toString)
          BadRequest("Permission denied")

      }
    }

  }

  def getAnnotation(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      findJobAnnotation(BSONDocument(JobAnnotation.JOBID -> jobID)).flatMap {

        case Some(x) =>
          findJob(BSONDocument(Job.JOBID -> jobID)).map { jobList =>
            if (jobList.get.ownerID.get == user.userID) {

              Ok(x.content)

            } else BadRequest("Permission denied")

          }

        case None =>
          findJob(BSONDocument(Job.JOBID -> jobID)).map { jobList =>
            if (jobList.get.ownerID.get == user.userID) {

              Ok

            } else BadRequest("Permission denied")

          }

      }
    }
  }

  /**
    * checks given key against the key that is
    * located in the folder jobPath/jobID/key
    *
    * @param jobID
    * @param key
    * @return
    */
  def checkKey(jobID: String, key: String): Boolean = {
    val refKey = Source.fromFile(jobPath + "/" + jobID + "/key").mkString.replaceAll("\n", "")
    key == refKey
  }
}
