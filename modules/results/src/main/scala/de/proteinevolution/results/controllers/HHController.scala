package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.results.models.{ HHContext, ResultsForm }
import de.proteinevolution.results.results.General.DTParam
import de.proteinevolution.results.services.HHService
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
        val config = Map("draw" -> params.draw, "recordsTotal" -> result.num_hits, "recordsFiltered" -> hits.length)
        val data = "data" -> hits
          .slice(params.displayStart, params.displayStart + params.pageLength)
          .map(_.toDataTable(result.db))
        Ok(config.asJson.deepMerge(data.asJson))
      case Left(_) => BadRequest
    }
  }

}
