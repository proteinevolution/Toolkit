package de.proteinevolution.jobs.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.actors.JobActor.{ JobStateChanged, SetSGEID }
import de.proteinevolution.jobs.services.JobActorAccess
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.JobState.{ Done, Error, Queued, Running }
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import better.files._
import play.api.Logger

@Singleton
class ClusterApiController @Inject()(constants: ConstantsV2, jobActorAccess: JobActorAccess, cc: ControllerComponents)
    extends ToolkitController(cc) {

  private val logger = Logger("job.states")

  def setJobStatus(status: String, jobID: String, key: String) = Action {
    if (checkKey(jobID, key)) {
      val jobStatus = status match {
        case "done"    => Done
        case "error"   => logger.warn(s"$jobID reached error state."); Error
        case "queued"  => Queued
        case "running" => Running
      }
      jobActorAccess.sendToJobActor(jobID, JobStateChanged(jobID, jobStatus))
      NoContent
    } else BadRequest("Permission denied")
  }

  def setSgeId(jobID: String, sgeID: String, key: String): Action[AnyContent] = Action {
    if (checkKey(jobID, key)) {
      jobActorAccess.sendToJobActor(jobID, SetSGEID(jobID, sgeID))
      NoContent
    } else BadRequest("Permission denied")
  }

  private def checkKey(jobID: String, key: String): Boolean = {
    (for {
      in <- File(constants.jobPath + "/" + jobID + "/key").newInputStream.autoClosed
    } yield {
      in.lines.mkString.replaceAll("\n", "") == key
    }).get()
  }

}
