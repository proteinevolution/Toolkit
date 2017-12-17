package controllers

import javax.inject.Inject

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.Constants
import de.proteinevolution.models.database.results.General.DTParam
import de.proteinevolution.models.database.results.HHomp
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.database.results.HHomp.{ HHompHSP, HHompResult }
import play.api.Logger
import play.api.libs.json.{ JsObject, Json }
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }
import scala.sys.process._

/**
 *
 * HHpred Controller process all requests
 * made from the HHpred result view
 */
class HHompController @Inject()(resultFiles: ResultFileAccessor,
                                hhomp: HHomp,
                                constants: Constants,
                                cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with CommonController {

  /* gets the path to all scripts that are executed
     on the server (not executed on the grid eninge) */
  private val serverScripts           = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignmentHHomp.sh").toFile

  /**
   * Retrieves the template alignment for a given
   * accession, therefore it runs a script on the server
   * (now grid engine) and writes it to the current job folder
   * to 'accession'.fas
   *
   * @param jobID
   * @param accession
   * @return Http response
   */
  def retrieveTemplateAlignment(jobID: String, accession: String): Action[AnyContent] = Action.async {
    if (jobID.isEmpty || accession.isEmpty) {
      Logger.info("either job or accession is empty")
    }
    if (!templateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
    } else {
      Future.successful {
        Process(templateAlignmentScript.pathAsString,
                (constants.jobPath + jobID).toFile.toJava,
                "jobID"     -> jobID,
                "accession" -> accession).run().exitValue() match {
          case 0 => Ok
          case _ => BadRequest
        }
      }
    }
  }

  /**
   * given dataTable specific paramters, this function
   * filters for eg. a specific column and returns the data
   * @param hits
   * @param params
   * @return
   */
  def getHitsByKeyWord(hits: HHompResult, params: DTParam): List[HHompHSP] = {
    if (params.sSearch.isEmpty) {
      hits.hitsOrderBy(params).slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
    } else {
      hits.hitsOrderBy(params).filter(_.description.contains(params.sSearch))
    }
  }

  /**
   * Retrieves hit rows (String containing Html)
   * for the alignment section in the result view
   * for a given range (start, end). Those can be either
   * wrapped or unwrapped, colored or uncolored
   *
   * Expects json sent by POST including:
   *
   * start: index of first HSP that is retrieved
   * end: index of last HSP that is retrieved
   * wrapped: Boolean true = wrapped, false = unwrapped
   * isColored: Boolean true = colored, false = uncolored
   *
   * @param jobID
   * @return Https response: HSP row(s) as String
   */
  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val start   = (json \ "start").as[Int]
    val end     = (json \ "end").as[Int]
    val isColor = (json \ "isColor").as[Boolean]
    val wrapped = (json \ "wrapped").as[Boolean]
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = hhomp.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits =
            result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hhomp.hit(_, isColor, wrapped))
          Ok(hits.mkString)
        }
    }
  }

  /**
   * this method fetches the data for the PSIblast hitlist
   * datatable
   *
   * @param jobID
   * @return
   */
  def dataTable(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc")
    )
    resultFiles.getResults(jobID).map {
      case None => NotFound
      case Some(jsValue) =>
        val result = hhomp.parseResult(jsValue)
        val hits   = getHitsByKeyWord(result, params)
        Ok(
          Json
            .toJson(Map("iTotalRecords" -> result.num_hits, "iTotalDisplayRecords" -> result.num_hits))
            .as[JsObject]
            .deepMerge(Json.obj("aaData" -> hits.map(_.toDataTable)))
        )
    }
  }
}
