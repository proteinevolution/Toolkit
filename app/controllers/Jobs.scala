package controllers

import javax.inject.{Inject, Singleton}

import actors.JobActor.{JobStateChanged, UpdateLog}
import models.{Constants, UserSessions}
import models.database.jobs._
import models.job.JobActorAccess
import modules.LocationProvider
import modules.db.MongoStore
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.api.mvc._
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}

import scala.io.Source
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * This controller is supposed to handle request coming from the Backend, such as compute
  * nodes from a gridengine. It checks if the posted key matches up with the key that is stored in
  * each job folder in order to prevent unauthorized status changes.
  *
  */
@Singleton
final class Jobs @Inject()(jobActorAccess: JobActorAccess,
                           userSessions: UserSessions,
                           @NamedCache("userCache") implicit val userCache: CacheApi,
                           implicit val locationProvider: LocationProvider,
                           mongoStore: MongoStore,
                           constants: Constants)
    extends Controller {

  def jobStatusDone(jobID: String, key: String) = Action {

    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Done))
      Ok("done")
    } else BadRequest("Permission denied")
  }

  def jobStatusError(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Error))
      Ok("error")
    } else BadRequest("Permission denied")
  }

  def jobStatusRunning(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Running))
      Ok("running")
    } else BadRequest("Permission denied")
  }

  def jobStatusQueued(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Queued))
      Ok("queued")
    } else BadRequest("Permission denied")
  }

  def updateLog(jobID: String) = Action {
    jobActorAccess.sendToJobActor(jobID, UpdateLog(jobID)) // TODO somehow this is getting triggered too rarely
    Ok
  }

  def SGEID(jobID: String, sgeID: String): Action[AnyContent] = Action.async {

    mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).map {

      case Some(job) =>
        mongoStore.modifyJob(BSONDocument(Job.JOBID -> job.jobID),
                             BSONDocument("$set"    -> BSONDocument("clusterData.sgeid" -> sgeID)))
        Logger.info(jobID + " gets job-ID " + sgeID + " on SGE")
        Ok
      case None =>
        Logger.info("Unknown ID " + jobID.toString)
        BadRequest
    }

  }

  def pushMessage(jobID: String, message: String) = Action {
    //userManager ! RunningJobMessage(reactivemongo.bson.BSONObjectID.parse(jobID).get, message)
    Ok
  }

  // TODO make secure

  def updateDateViewed(jobID: String) = Action {

    mongoStore.modifyJob(
      BSONDocument(Job.JOBID -> jobID),
      BSONDocument("$set"    -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(DateTime.now().getMillis)))
    )
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
    userSessions.getUser.flatMap { user =>
      mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).map {

        case x if x.get.ownerID.get == user.userID =>
          val entry = JobAnnotation(mainID = BSONObjectID.generate(),
                                    jobID = jobID,
                                    content = content,
                                    dateCreated = Some(DateTime.now()))

          mongoStore.upsertAnnotation(entry)

          mongoStore.modifyAnnotation(BSONDocument(JobAnnotation.JOBID -> jobID),
                                      BSONDocument("$set"              -> BSONDocument(JobAnnotation.CONTENT -> content)))
          Ok("annotation upserted")

        case _ =>
          Logger.info("Unknown ID " + jobID.toString)
          BadRequest("Permission denied")

      }
    }

  }

  def getAnnotation(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      mongoStore.findJobAnnotation(BSONDocument(JobAnnotation.JOBID -> jobID)).flatMap {

        case Some(x) =>
          mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).map { jobList =>
            if (jobList.get.ownerID.get == user.userID) {

              Ok(x.content)

            } else BadRequest("Permission denied")

          }

        case None =>
          mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).map { jobList =>
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
    val source = Source.fromFile(constants.jobPath + "/" + jobID + "/key")
    val refKey = try { source.mkString.replaceAll("\n", "") } finally { source.close() }
    key == refKey
  }
}
