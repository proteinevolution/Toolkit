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
import de.proteinevolution.jobs.models.{HHContext, ResultsForm}
import de.proteinevolution.jobs.services.HHService
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.ExecutionContext

@Singleton
class HHController @Inject()(
    ctx: HHContext,
    service: HHService,
    jobDao: JobDao,
    userAction: UserAction
)(implicit ec: ExecutionContext)
    extends ToolkitController(ctx.controllerComponents) {

  def loadHits(
      jobID: String,
      start: Option[Int],
      end: Option[Int],
      filter: Option[String],
      sortBy: Option[String],
      desc: Option[Boolean]
  ): Action[AnyContent] = userAction.async { implicit request =>
    jobDao.findJob(jobID).flatMap {
      case Some(job) =>
        if (job.isPublic || job.ownerID.equals(request.user.userID)) {
          val form = ResultsForm(start, end, filter, sortBy, desc)
          service.loadHits(jobID, form).value.map {
            case Right(json) => Ok(json)
            case Left(_)     => BadRequest
          }
        } else {
          fuccess(Unauthorized)
        }
      case _ => fuccess(NotFound)
    }
  }
}
