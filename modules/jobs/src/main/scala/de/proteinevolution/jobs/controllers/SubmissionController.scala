package de.proteinevolution.jobs.controllers

import java.time.ZonedDateTime

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.actors.JobActor.{ CheckIPHash, Delete }
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.services.JobActorAccess
import de.proteinevolution.models.database.jobs.JobState.Done
import de.proteinevolution.models.database.statistics.{ JobEvent, JobEventLog }
import de.proteinevolution.services.ToolConfig
import javax.inject.Inject
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

class SubmissionController @Inject()(
    jobActorAccess: JobActorAccess,
    userSessions: UserSessions,
    cc: ControllerComponents,
    jobDao: JobDao,
    toolConfig: ToolConfig
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  def startJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { _ =>
      jobActorAccess.sendToJobActor(jobID, CheckIPHash(jobID))
      Ok(Json.toJson(Json.obj("message" -> "Starting Job...")))
    }
  }

  def frontend(toolName: String): Action[AnyContent] = Action.async { implicit request =>
    if (toolConfig.isTool(toolName)) {
      // Add Frontend Job to Database
      val jobLog =
        JobEventLog(toolName = toolName.trim.toLowerCase, events = JobEvent(Done, Some(ZonedDateTime.now)) :: Nil)
      jobDao.addJobLog(jobLog).map { _ =>
        NoContent
      }
    } else {
      Future.successful(BadRequest)
    }
  }

  def delete(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info("Delete Action in JobController reached")
    userSessions.getUser.map { user =>
      jobActorAccess.sendToJobActor(jobID, Delete(jobID, Some(user.userID)))
      Ok
    }
  }

}
