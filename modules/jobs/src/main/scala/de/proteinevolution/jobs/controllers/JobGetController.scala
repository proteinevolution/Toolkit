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

import better.files._
import cats.implicits._
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.jobs.JobState.Done
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.{ Job, JobHashError }
import de.proteinevolution.jobs.services.{ JobFolderValidation, JobHashCheckService, ResultViewFactory }
import de.proteinevolution.tools.{ Tool, ToolConfig }
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import play.api.{ Configuration, Logging }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobGetController @Inject()(
    jobHashService: JobHashCheckService,
    userSessions: UserSessionService,
    resultViewFactory: ResultViewFactory,
    jobDao: JobDao,
    cc: ControllerComponents,
    toolConfig: ToolConfig,
    constants: ConstantsV2,
    userAction: UserAction
)(implicit ec: ExecutionContext, config: Configuration)
    extends ToolkitController(cc)
    with JobFolderValidation
    with Logging {

  def getAllJobs: Action[AnyContent] = userAction.async { implicit request =>
    jobDao.findJobsByOwnerOrPublicWatched(request.user.userID, request.user.jobs).map { jobs =>
      Ok(jobs.filter(job => jobFolderIsValid(job.jobID, constants)).map(_.jsonPrepare(toolConfig, request.user)).asJson)
    }
  }

  def suggestJobsForQuery(queryString: String): Action[AnyContent] = userAction.async { implicit request =>
    val user = request.user
    val tools: List[Tool] = toolConfig.values.values
      .filter(t => queryString.toLowerCase.r.findFirstIn(t.toolNameLong.toLowerCase()).isDefined)
      .filterNot(_.toolNameShort == "hhpred_manual")
      .toList

    jobDao.findJobsByAutocomplete(user.userID, user.jobs, queryString, tools.map(_.toolNameShort)).map { jobs =>
      Ok(jobs.map(_.jsonPrepare(toolConfig, request.user)).asJson)
    }
  }

  def loadJob(jobID: String): Action[AnyContent] = userAction.async { implicit request =>
    jobDao.findJob(jobID).flatMap {
      case Some(job) if jobFolderIsValid(job.jobID, constants) =>
        if (job.isPublic || job.ownerID.equals(request.user.userID)) {
          val paramValues: Map[String, String] = {
            if (paramsExist(jobID, constants)) {
              (constants.jobPath / jobID / "sparam").readDeserialized[Map[String, String]]()
            } else {
              Map.empty[String, String]
            }
          }
          jobViews(job).map { views =>
            Ok(job.jsonPrepare(toolConfig, request.user, Some(paramValues), Some(views)).asJson)
          }
        } else {
          fuccess(Unauthorized)
        }
      case _ => fuccess(NotFound)
    }
  }

  private def jobViews(job: Job): Future[Seq[String]] = job.status match {
    case Done =>
      resultViewFactory(job.tool, job.jobID).value.map {
        case Right(r) => r.tabs.keys.toSeq
        case Left(_) =>
          logger.error(s"no views found for $job")
          Nil
      }
    case _ => fuccess(Nil)
  }

  def checkHash(jobID: String): Action[AnyContent] = userAction.async { implicit request =>
    (for {
      job <- jobHashService.checkHash(jobID)
    } yield {
      (job.jobID, job.dateCreated.toInstant.toEpochMilli)
    }).value.map {
      case Some((latestJobId, dateCreated)) =>
        Ok(JsonObject("jobID" -> Json.fromString(latestJobId), "dateCreated" -> Json.fromLong(dateCreated)).asJson)
      case None => NotFound(errors(JobHashError.JobNotFound.msg))
    }
  }

}
