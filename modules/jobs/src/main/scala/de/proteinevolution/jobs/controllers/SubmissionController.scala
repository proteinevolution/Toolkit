package de.proteinevolution.jobs.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.actors.JobActor.{ CheckIPHash, Delete }
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.services._
import de.proteinevolution.tools.ToolConfig
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.libs.Files
import play.api.mvc.{ Action, AnyContent, ControllerComponents, MultipartFormData }
import io.circe.JsonObject
import io.circe.Json
import io.circe.syntax._

import scala.concurrent.ExecutionContext

@Singleton
class SubmissionController @Inject()(
    jobActorAccess: JobActorAccess,
    userSessions: UserSessions,
    jobDispatcher: JobDispatcher,
    cc: ControllerComponents,
    jobDao: JobDao,
    toolConfig: ToolConfig,
    jobResubmitService: JobResubmitService,
    jobIdProvider: JobIdProvider,
    jobFrontendToolsService: JobFrontendToolsService
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  def startJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { _ =>
      jobActorAccess.sendToJobActor(jobID, CheckIPHash(jobID))
      Ok(JsonObject("message" -> Json.fromString("Starting Job...")).asJson)
    }
  }

  def frontend(toolName: String): Action[AnyContent] = Action.async { implicit request =>
    if (toolConfig.isTool(toolName)) {
      // Add Frontend Job to Database
      jobFrontendToolsService.logFrontendJob(toolName).map(_ => NoContent)
    } else {
      fuccess(BadRequest)
    }
  }

  def delete(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    logger.info("Delete Action in JobController reached")
    userSessions.getUser.map { user =>
      jobActorAccess.sendToJobActor(jobID, Delete(jobID, Some(user.userID)))
      NoContent
    }
  }

  def submitJob(toolName: String): Action[MultipartFormData[Files.TemporaryFile]] =
    Action(parse.multipartFormData).async { implicit request =>
      userSessions.getUser.flatMap { user =>
        jobDispatcher
          .submitJob(
            toolName,
            request.body.dataParts,
            request.body.file("files").filter(_.contentType.contains("text/plain")),
            user
          )
          .value
          .map {
            case Right(job) =>
              Ok(
                JsonObject(
                  "successful" -> Json.fromBoolean(true),
                  "code"       -> Json.fromInt(0),
                  "message"    -> Json.fromString("Submission successful."),
                  "jobID"      -> Json.fromString(job.jobID)
                ).asJson
              ).withSession(userSessions.sessionCookie(request, user.sessionID.get))
            case Left(error) => BadRequest(errors(error.msg))
          }
      }
    }

  def resubmitJob(newJobID: String, resubmitForJobID: Option[String]): Action[AnyContent] = Action.async {
    implicit request =>
      jobResubmitService.resubmit(newJobID, resubmitForJobID).map(r => Ok(r.asJson))
  }

}
