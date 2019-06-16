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

import java.time.ZonedDateTime

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.JobHashError
import de.proteinevolution.jobs.services.{ JobFolderValidation, JobHashCheckService }
import de.proteinevolution.tools.ToolConfig
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.ExecutionContext

@Singleton
class JobGetController @Inject()(
    jobHashService: JobHashCheckService,
    userSessions: UserSessions,
    jobDao: JobDao,
    cc: ControllerComponents,
    toolConfig: ToolConfig,
    constants: ConstantsV2
)(implicit ec: ExecutionContext, config: Configuration)
    extends ToolkitController(cc)
    with JobFolderValidation {

  def jobManagerListJobs: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      jobDao.findJobsByOwner(user.userIDDB).map {
        jobs =>
          NoCache(Ok(jobs.filter(job => jobFolderIsValid(job.jobID, constants)).map(_.jobManagerJob()).asJson))
      }
    }
  }

  def listJobs: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      jobDao.findJobsByIds(user.jobs).map { jobs =>
        Ok(jobs.filter(job => jobFolderIsValid(job.jobID, constants)).map(_.cleaned(toolConfig)).asJson)
      }
    }
  }

  def loadJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { _ =>
      jobDao.findJob(jobID).map {
        case Some(job) if jobFolderIsValid(job.jobID, constants) => Ok(job.cleaned(toolConfig).asJson)
        case _                                                   => NotFound
      }
    }
  }

  def checkHash(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    (for {
      _   <- OptionT.liftF(userSessions.getUser)
      job <- jobHashService.checkHash(jobID)
    } yield {
      (job.jobID, job.dateCreated.getOrElse(ZonedDateTime.now).toInstant.toEpochMilli)
    }).value.map {
      case Some((latestJobId, dateCreated)) =>
        Ok(JsonObject("jobID" -> Json.fromString(latestJobId), "dateCreated" -> Json.fromLong(dateCreated)).asJson)
      case None => NotFound(errors(JobHashError.JobNotFound.msg))
    }
  }

}
