package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.results.results.Common
import javax.inject.Inject
import io.circe.parser._
import io.circe._
import play.api.mvc.{ AnyContent, ControllerComponents }
import play.mvc.Action

import scala.concurrent.ExecutionContext

class AlignmentController @Inject()(
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  def getAln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json       = request.body.asJson
    val resultName = (json \ "resultName").as[String]
    val numList    = (json \ "checkboxes").as[List[Int]]
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(json) =>

        Ok(fas.mkString)
    }
  }

  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json       = request.body.asJson.get
    val start      = (json \ "start").as[Int]
    val end        = (json \ "end").as[Int]
    val resultName = (json \ "resultName").as[String]
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = Alignment.parse((jsValue \ resultName).as[JsArray])
        if (end > result.alignment.length || start > result.alignment.length) {
          BadRequest
        } else {
          val hits = result.alignment.slice(start, end).map(views.html.alignment.alignmentrow(_))
          Ok(hits.mkString)
        }
    }
  }

  def loadHitsClustal(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json       = request.body.asJson.get
    val resultName = (json \ "resultName").as[String]
    val color      = (json \ "color").as[Boolean]
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = Alignment.parse((jsValue \ resultName).as[JsArray])
        val hits   = Common.clustal(result, 0, constants.breakAfterClustal, color)
        Ok(hits.mkString)
    }
  }

}
