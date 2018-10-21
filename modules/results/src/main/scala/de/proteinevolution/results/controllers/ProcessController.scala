package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.models.ToolName._
import de.proteinevolution.models.ToolName
import de.proteinevolution.results.models.{ ForwardMode, ForwardingData, HHContext }
import de.proteinevolution.results.results.{ HSP, SearchResult }
import de.proteinevolution.results.services.{ ProcessFactory, ProcessService, ResultsRepository }
import javax.inject.{ Inject, Singleton }
import play.api.libs.concurrent.Futures
import play.api.libs.concurrent.Futures._
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class ProcessController @Inject()(
    ctx: HHContext,
    service: ProcessService
)(implicit ec: ExecutionContext, futures: Futures)
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
