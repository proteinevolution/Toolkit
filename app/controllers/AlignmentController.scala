package controllers

import javax.inject.Inject

import de.proteinevolution.models.Constants
import de.proteinevolution.tools.results.{ Alignment, Common }
import de.proteinevolution.db.ResultFileAccessor
import play.api.libs.json.JsArray
import play.api.mvc._

import scala.concurrent.ExecutionContext

class AlignmentController @Inject()(resultFiles: ResultFileAccessor,
                                    aln: Alignment,
                                    constants: Constants,
                                    cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with CommonController {

  /*
  TODO : THIS CONTROLLER MUST GO AS WELL
   */

  /**
   * Retrieves an alignment from a file
   * within the result folder with the filename '@resultName'.json
   * for an given array containing the numbers of the alignments
   *
   * Expects json sent by POST including:
   *
   * resultName: alignment within the result folder with the filename '@resultName'.json
   * checkboxes: an array which contains the numbers (in the Alignment list)
   * of all alignments that will be retrieved
   *
   * @param jobID
   * @return alignment as fasta
   */
  def getAln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json       = request.body.asJson.get
    val resultName = (json \ "resultName").as[String]
    val numList    = (json \ "checkboxes").as[List[Int]]
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = aln.parse((jsValue \ resultName).as[JsArray]).alignment
        val fas = numList.map { num =>
          ">" + result { num - 1 }.accession + "\n" + result { num - 1 }.seq + "\n"
        }
        Ok(fas.mkString)
    }
  }

  /**
   * Retrieves alignment rows (String containing Html)
   * for the alignment section in the result view
   * for a given range (start, end).
   *
   * Expects json sent by POST including:
   *
   * start: index of first HSP that is retrieved
   * end: index of last HSP that is retrieved
   *
   * @param jobID
   * @return HSP row(s) as String
   */
  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json       = request.body.asJson.get
    val start      = (json \ "start").as[Int]
    val end        = (json \ "end").as[Int]
    val resultName = (json \ "resultName").as[String]
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = aln.parse((jsValue \ resultName).as[JsArray])
        if (end > result.alignment.length || start > result.alignment.length) {
          BadRequest
        } else {
          val hits = result.alignment.slice(start, end).map(views.html.jobs.resultpanels.alignmentRow(_))
          Ok(hits.mkString)
        }
    }
  }

  /**
   * Retrieves an alignment in clustal format
   * for a given range (start, end).
   *
   * Expects json sent by POST including:
   *
   * start: index of first HSP that is retrieved
   * end: index of last HSP that is retrieved
   *
   * @param jobID
   * @return the whole alignment containing Html as a String
   */
  def loadHitsClustal(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json       = request.body.asJson.get
    val resultName = (json \ "resultName").as[String]
    val color      = (json \ "color").as[Boolean]
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = aln.parse((jsValue \ resultName).as[JsArray])
        val hits   = Common.clustal(result, 0, constants.breakAfterClustal, color)
        Ok(hits.mkString)
    }
  }
}
