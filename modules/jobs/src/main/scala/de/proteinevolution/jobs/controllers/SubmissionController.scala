/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.jobs.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.actors.JobActor.{ CheckIPHash, Delete }
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.services._
import de.proteinevolution.tools.ToolConfig
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Singleton }
import play.api.Logging
import play.api.libs.Files
import play.api.mvc.{ Action, AnyContent, ControllerComponents, MultipartFormData }
import play.mvc.Http.MimeTypes

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
    extends ToolkitController(cc)
    with Logging {

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
      jobActorAccess.sendToJobActor(jobID, Delete(jobID, Some(user.userIDDB)))
      NoContent
    }
  }

  // TODO the MultipartFormData contains a dynamic Map which should be modelled properly, issue #705
  def submitJob(toolName: String): Action[MultipartFormData[Files.TemporaryFile]] =
    Action(parse.multipartFormData).async { implicit request =>
      userSessions.getUser.flatMap { user =>
        jobDispatcher
          .submitJob(
            toolName,
            request.body.dataParts,
            request.body.files.filter(f => java.nio.file.Files.probeContentType(f.ref.path).contains(MimeTypes.TEXT)),
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
