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
import de.proteinevolution.results.services.ResultGetService
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.ExecutionContext

@Singleton
class ResultGetController @Inject()(cc: ControllerComponents, resultGetService: ResultGetService)(
    implicit ec: ExecutionContext
) extends ToolkitController(cc) {

  def get(jobID: String, tool: String, resultView: String): Action[AnyContent] = Action.async { implicit request =>
    resultGetService.get(jobID, tool, resultView).value.map {
      case Right(view) => Ok(view.body)
      case Left(_)     => Ok(views.html.errors.resultnotfound())
    }
  }

  def getJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    resultGetService.getJob(jobID).value.map {
      case Some(job) => Ok(job.asJson)
      case None      => NotFound
    }
  }

}
