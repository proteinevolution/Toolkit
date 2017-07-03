package controllers

import javax.inject.Inject

import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext.Implicits.global
import models.Constants
import models.database.results.{Alignment, AlignmentResult, General}
import models.results.BlastVisualization
import modules.db.MongoStore
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import play.api.libs.json.JsArray

/**
  * Created by drau on 28.04.17.
  */
class AlignmentController @Inject()(aln: Alignment,
                                    general: General,
                                    mongoStore: MongoStore,
                                    val reactiveMongoApi: ReactiveMongoApi,
                                    constants: Constants)
    extends Controller
    with Common
    with ReactiveMongoComponents {

  def getAln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val resultName  = (json \ "resultName").as[String]
    val numList = (json \ "checkboxes").as[List[Int]]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = aln.parseAlignment((jsValue \ resultName).as[JsArray]).alignment
        val fas = numList.map { num =>
          ">" + result { num - 1 }.accession + "\n" + result { num - 1 }.seq + "\n"
        }
        Ok(fas.mkString)
      case None => BadRequest
    }
  }

  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json             = request.body.asJson.get
    val start            = (json \ "start").as[Int]
    val end              = (json \ "end").as[Int]
    val resultName       = (json \ "resultName").as[String]
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result = aln.parseAlignment((jsValue \ resultName).as[JsArray])
          if (end > result.alignment.length || start > result.alignment.length) {
            BadRequest
          } else {
            val hits = result.alignment.slice(start, end).map(views.html.jobs.resultpanels.alignmentRow(_))
            Ok(hits.mkString)
          }
      }
  }



  def loadHitsClustal(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val resultName  = (json \ "resultName").as[String]
    val color      = (json \ "color").as[Boolean]
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result = aln.parseAlignment((jsValue \ resultName).as[JsArray])
            val hits = BlastVisualization.clustal(result, 0, constants.breakAfterClustal, color)
            Ok(hits.mkString)
          }
      }

}
