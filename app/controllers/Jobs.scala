package controllers

import java.time.ZonedDateTime
import javax.inject.{ Inject, Singleton }

import de.proteinevolution.models.database.jobs.JobState._
import actors.JobActor.{ JobStateChanged, SetSGEID, UpdateLog }
import de.proteinevolution.common.LocationProvider
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs._
import de.proteinevolution.db.MongoStore
import play.api.cache.{ NamedCache, SyncCacheApi }
import play.api.mvc._
import reactivemongo.bson.{ BSONDateTime, BSONDocument }
import services.JobActorAccess

import scala.io.Source

/**
 * This controller is supposed to handle request coming from the Backend, such as compute
 * nodes from a gridengine. It checks if the posted key matches up with the key that is stored in
 * each job folder in order to prevent unauthorized status changes.
 *
 */
@Singleton
final class Jobs @Inject()(jobActorAccess: JobActorAccess,
                           @NamedCache("userCache") implicit val userCache: SyncCacheApi,
                           implicit val locationProvider: LocationProvider,
                           mongoStore: MongoStore,
                           constants: ConstantsV2,
                           cc: ControllerComponents)
    extends AbstractController(cc) {

  def jobStatusDone(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Done))
      NoContent
    } else BadRequest("Permission denied")
  }

  def jobStatusError(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Error))
      NoContent
    } else BadRequest("Permission denied")
  }

  def jobStatusRunning(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Running))
      NoContent
    } else BadRequest("Permission denied")
  }

  def jobStatusQueued(jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, Queued))
      NoContent
    } else BadRequest("Permission denied")
  }

  def updateLog(jobID: String) = Action {
    jobActorAccess.sendToJobActor(jobID, UpdateLog(jobID)) // TODO somehow this is getting triggered too rarely
    NoContent
  }

  def SGEID(jobID: String, sgeID: String, key: String): Action[AnyContent] = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, SetSGEID(jobID, sgeID))
      NoContent
    } else BadRequest("Permission denied")
  }

  def updateDateViewed(jobID: String) = Action {
    mongoStore.modifyJob(
      BSONDocument(Job.JOBID -> jobID),
      BSONDocument("$set"    -> BSONDocument(Job.DATEVIEWED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)))
    )
    NoContent
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
