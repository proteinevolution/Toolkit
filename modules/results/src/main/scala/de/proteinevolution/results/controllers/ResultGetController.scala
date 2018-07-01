package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.results.services.ResultGetService
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.{ JsString, Json }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.ExecutionContext

@Singleton
class ResultGetController @Inject()(cc: ControllerComponents, resultGetService: ResultGetService)(
    implicit ec: ExecutionContext
) extends ToolkitController(cc) {

  def get(jobId: String, tool: String, resultView: String): Action[AnyContent] = Action.async { implicit request =>
    resultGetService.get(jobId, tool, resultView).map(view => Ok(JsString(view.body)))
  }

  def getJob(jobId: String): Action[AnyContent] = Action.async { implicit request =>
    resultGetService.getJob(jobId).value.map {
      case Some(job) => Ok(Json.toJson(job))
      case None      => NotFound
    }
  }

}
