package controllers

import java.time.ZonedDateTime
import javax.inject.{ Inject, Singleton }

import de.proteinevolution.models.database.jobs.JobState._
import actors.JobActor.{ JobStateChanged, SetSGEID, UpdateLog }
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs._
import de.proteinevolution.db.MongoStore
import play.api.mvc._
import reactivemongo.bson.{ BSONDateTime, BSONDocument }
import services.JobActorAccess

import scala.io.Source

@Singleton
final class Jobs @Inject()(
    jobActorAccess: JobActorAccess,
    mongoStore: MongoStore,
    constants: ConstantsV2,
    cc: ControllerComponents
) extends AbstractController(cc) {

  def setJobStatus(status: String, jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      val jobStatus = status match {
        case "done"    => Done
        case "error"   => Error
        case "queued"  => Queued
        case "running" => Running
      }
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, jobStatus))
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

  def checkKey(jobID: String, key: String): Boolean = {
    val source = Source.fromFile(constants.jobPath + "/" + jobID + "/key")
    val refKey = try { source.mkString.replaceAll("\n", "") } finally { source.close() }
    key == refKey
  }
}
