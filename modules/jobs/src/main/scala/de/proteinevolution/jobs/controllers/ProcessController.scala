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

import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.HHContext
import de.proteinevolution.jobs.services.ProcessService
import de.proteinevolution.jobs.models.ForwardingData
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

@Singleton
class ProcessController @Inject()(
    ctx: HHContext,
    jobDao: JobDao,
    service: ProcessService,
    userAction: UserAction
)(implicit ec: ExecutionContext)
    extends ToolkitController(ctx.controllerComponents) {

  def templateAlignment(jobID: String, accession: String): Action[AnyContent] = userAction.async { implicit request =>
    jobDao.findJob(jobID).flatMap {
      case Some(job) =>
        if (job.isPublic || job.ownerID.equals(request.user.userID)) {
          service.templateAlignment(jobID, accession).value.map {
            case Some(0) => NoContent
            case _       => BadRequest
          }
        } else {
          fuccess(Unauthorized)
        }
      case _ => fuccess(NotFound)
    }
  }

  def forwardAlignment(jobID: String): Action[ForwardingData] =
    userAction((circe.json[ForwardingData])).async { implicit request =>
      jobDao.findJob(jobID).flatMap {
        case Some(job) =>
          if (job.isPublic || job.ownerID.equals(request.user.userID)) {
            service.forwardAlignment(jobID, request.body).value.map {
              case Right(res) => Ok.sendFile(res)
              case _          => BadRequest
            }
          } else {
            fuccess(Unauthorized)
          }
        case _ => fuccess(NotFound)
      }
    }

}
