package controllers

import java.time.ZonedDateTime
import javax.inject.{ Inject, Singleton }

import actors.JobActor.{ JobStateChanged, SetSGEID, UpdateLog }
import models.{ Constants, UserSessions }
import models.database.jobs._
import modules.LocationProvider
import modules.db.MongoStore
import play.api.Logger
import play.api.cache.{ NamedCache, SyncCacheApi }
import play.api.mvc._
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONObjectID }
import services.JobActorAccess

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
                           @NamedCache("userCache") implicit val userCache: SyncCacheApi,
                           implicit val locationProvider: LocationProvider,
                           mongoStore: MongoStore,
                           constants: Constants,
                           cc: ControllerComponents)
    extends AbstractController(cc) {

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

  def SGEID(jobID: String, sgeID: String, key : String): Action[AnyContent] = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, SetSGEID(jobID, sgeID))
      Ok
    } else BadRequest("Permission denied")
  }

  def pushMessage(jobID: String, message: String) = Action {
    //userManager ! RunningJobMessage(reactivemongo.bson.BSONObjectID.parse(jobID).get, message)
    Ok
  }

  // TODO make secure

  def updateDateViewed(jobID: String) = Action {

    mongoStore.modifyJob(
      BSONDocument(Job.JOBID -> jobID),
      BSONDocument("$set"    -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)))
    )
    Ok
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
