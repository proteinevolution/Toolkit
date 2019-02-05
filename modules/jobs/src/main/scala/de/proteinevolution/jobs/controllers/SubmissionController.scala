package de.proteinevolution.jobs.controllers
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.actors.JobActor.{ CheckIPHash, Delete }
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.services._
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.tools.ToolConfig
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.ExecutionContext

@Singleton
class SubmissionController @Inject()(
    jobActorAccess: JobActorAccess,
    userSessions: UserSessionService,
    jobDispatcher: JobDispatcher,
    constants: ConstantsV2,
    cc: ControllerComponents,
    jobDao: JobDao,
    toolConfig: ToolConfig,
    jobResubmitService: JobResubmitService,
    jobIdProvider: JobIdProvider,
    jobFrontendToolsService: JobFrontendToolsService,
    userAction: UserAction
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  def startJob(jobID: String): Action[AnyContent] = userAction { implicit request =>
    jobActorAccess.sendToJobActor(jobID, CheckIPHash(jobID))
    Ok(JsonObject("message" -> Json.fromString("Starting Job...")).asJson)
  }

  def frontend(toolName: String): Action[AnyContent] = Action.async { implicit request =>
    if (toolConfig.isTool(toolName)) {
      // Add Frontend Job to Database
      jobFrontendToolsService.logFrontendJob(toolName).map(_ => NoContent)
    } else {
      fuccess(BadRequest)
    }
  }

  def delete(jobID: String): Action[AnyContent] = userAction { implicit request =>
    logger.info("Delete Action in JobController reached")
    jobActorAccess.sendToJobActor(jobID, Delete(jobID, Some(request.user.userID)))
    NoContent
  }

  def submitJob(toolName: String): Action[Json] = userAction(circe.json).async { implicit request =>
    request.body.asObject match {
      case None => fuccess(BadRequest)
      case Some(obj) =>
        val parts: Iterable[(String, String)] = for {
          (key, json) <- obj.toIterable
          str = json.fold[String]("",
                                  bool => bool.toString,
                                  num => num.toString,
                                  identity,
                                  vec => vec.toString,
                                  obj => obj.toString)
        } yield (key, str)
        jobDispatcher
          .submitJob(
            toolName,
            parts.toMap,
            request.user
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
              ).withSession(userSessions.sessionCookie(request))
            case Left(error) => BadRequest(errors(error.msg))
          }
    }
  }

  def resubmitJob(newJobID: String, resubmitForJobID: Option[String]): Action[AnyContent] = Action.async {
    implicit request =>
      jobResubmitService.resubmit(newJobID, resubmitForJobID).map(r => Ok(r.asJson))
  }

}
