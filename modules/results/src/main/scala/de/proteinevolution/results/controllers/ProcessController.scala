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

package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.results.models.{ ForwardMode, ForwardingData, HHContext }
import de.proteinevolution.results.services.{ ProcessService, ResultsRepository }
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

@Singleton
class ProcessController @Inject()(
    ctx: HHContext,
    service: ProcessService
)(implicit ec: ExecutionContext)
    extends ToolkitController(ctx.controllerComponents)
    with ResultsRepository {

  def templateAlignment(jobId: String, accession: String): Action[AnyContent] = Action.async { implicit request =>
    service.templateAlignment(jobId, accession).value.map {
      case Some(0) => NoContent
      case _       => BadRequest
    }
  }

  def forwardAlignment(jobId: String, mode: ForwardMode): Action[ForwardingData] =
    Action(circe.json[ForwardingData]).async { implicit request =>
      service.forwardAlignment(jobId, mode, request.body).value.map {
        case Right(res) if res == 0 => NoContent
        case _                      => BadRequest
      }
    }

}
