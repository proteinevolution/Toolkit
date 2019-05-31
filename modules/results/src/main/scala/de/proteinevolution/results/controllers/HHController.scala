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
import de.proteinevolution.results.models.{ HHContext, ResultsForm }
import de.proteinevolution.results.results.General.DTParam
import de.proteinevolution.results.services.HHService
import io.circe.JsonObject
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext

@Singleton
class HHController @Inject()(
    ctx: HHContext,
    service: HHService
)(implicit ec: ExecutionContext)
    extends ToolkitController(ctx.controllerComponents) {

  def loadHits(jobId: String): Action[ResultsForm] = Action(circe.json[ResultsForm]).async { implicit request =>
    service.loadHits(jobId, request.body).value.map {
      case Right(hits) => Ok(hits.mkString)
      case Left(_)     => BadRequest
    }
  }

  def dataTable(jobId: String): Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("draw").getOrElse("1").toInt,
      request.getQueryString("search[value]").getOrElse(""),
      request.getQueryString("start").getOrElse("0").toInt,
      request.getQueryString("length").getOrElse("100").toInt,
      request.getQueryString("order[0][column]").getOrElse("1").toInt,
      request.getQueryString("order[0][dir]").getOrElse("asc")
    )
    service.dataTable(jobId, params).value.map {
      case Right((hits, result)) =>
        val config = Map(
          "draw"            -> params.draw.asJson,
          "recordsTotal"    -> result.num_hits.asJson,
          "recordsFiltered" -> hits.length.asJson
        )
        val data = Map(
          "data" -> hits
            .slice(params.displayStart, params.displayStart + params.pageLength)
            .map(_.toDataTable(result.db))
            .asJson
        )
        Ok(JsonObject.fromMap(config ++ data).asJson)
      case Left(_) => BadRequest
    }
  }

}
