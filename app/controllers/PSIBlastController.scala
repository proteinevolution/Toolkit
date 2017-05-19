/**
  * Created by drau on 01.03.17.
  */
package controllers

import com.typesafe.config.ConfigFactory

import scala.sys.process._
import better.files._
import models.Constants
import models.database.results._
import play.api.mvc.{Action, AnyContent, Controller}
import javax.inject.Inject
import play.api.data._
import play.api.data.Forms._

import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import modules.CommonModule
import play.api.data.Form
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.concurrent.ExecutionContext.Implicits.global

class PSIBlastController @Inject()(psiblast: PSIBlast, general: General, aln: Alignment)(
    webJarAssets: WebJarAssets,
    val reactiveMongoApi: ReactiveMongoApi)
    extends Controller
    with Constants
    with CommonModule
    with Common {

  private val serverScripts   = ConfigFactory.load().getString("serverScripts")
  private val retrieveFullSeq = (serverScripts + "/retrieveFullSeq.sh").toFile

  def evalFull(jobID: String, eval: String): Action[AnyContent] = Action.async { implicit request =>
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      getResult(jobID).map {
        case Some(jsValue) =>
          val result        = psiblast.parseResult(jsValue)
          val accessionsStr = getAccessionsEval(result, eval.toDouble)
          val db            = result.db
          Process(retrieveFullSeq.pathAsString,
                  (jobPath + jobID).toFile.toJava,
                  "jobID"         -> jobID,
                  "accessionsStr" -> accessionsStr,
                  "db"            -> db).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }

        case _ => NotFound
      }
    }
  }

  def full(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val numList = (json \ "checkboxes").as[List[Int]]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      getResult(jobID).map {
        case Some(jsValue) =>
          val result        = psiblast.parseResult(jsValue)
          val accessionsStr = getAccessions(result, numList)
          val db            = result.db
          Process(retrieveFullSeq.pathAsString,
                  (jobPath + jobID).toFile.toJava,
                  "jobID"         -> jobID,
                  "accessionsStr" -> accessionsStr,
                  "db"            -> db).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }

        case _ => NotFound
      }
    }
  }

  def alnEval(jobID: String, eval: String): Action[AnyContent] = Action.async { implicit request =>
    getResult(jobID).map {
      case Some(jsValue) => Ok(getAlnEval(psiblast.parseResult(jsValue), eval.toDouble))
      case _             => NotFound
    }
  }

  def aln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val numList = (json \ "checkboxes").as[List[Int]]
    getResult(jobID).map {
      case Some(jsValue) => Ok(getAln(aln.parseAlignment((jsValue \ "alignment").as[JsArray]), numList))
      case _             => NotFound
    }

  }

  def getAlnEval(result: PSIBlastResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.evalue < eval).map { hit =>
      // not hit-num -1 because alginments adds query (+1) to beginning of retrieved file
      ">" + result.alignment(hit.num).accession + "\n" + result.alignment(hit.num).seq + "\n"
    }
    fas.mkString
  }

  def getAln(alignment: AlignmentResult, numList: Seq[Int]): String = {
    val fas = numList.map { num =>
      // not hit-num -1 because alginments adds query (+1) to beginning of retrieved file
      ">" + alignment.alignment(num).accession + "\n" + alignment.alignment(num).seq + "\n"
    }
    fas.mkString
  }

  def getAccessions(result: PSIBlastResult, numList: Seq[Int]): String = {
    val fas = numList.map { num =>
      result.HSPS(num - 1).accession + " "
    }
    fas.mkString
  }
  def getAccessionsEval(result: PSIBlastResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.evalue < eval).map { _.accession + " " }
    fas.mkString
  }

  def getHitsByKeyWord(jobID: String, params: DTParam): Future[List[PSIBlastHSP]] = {
    if (params.sSearch.isEmpty) {
      getResult(jobID).map {
        case Some(result) =>
          psiblast
            .hitsOrderBy(params, psiblast.parseResult(result).HSPS)
            .slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    } else {
      ???
    }
    //case false => (for (s <- getHits if (title.startsWith(params.sSearch))) yield (s)).list
  }

  def loadHits(jobID: String, start: Int, end: Int): Action[AnyContent] = Action.async { implicit request =>
    getResult(jobID).map {
      case Some(jsValue) =>
        val result = psiblast.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits = result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.psiblast.hit(jobID, _, result.db))
          Ok(hits.mkString)
        }

    }
  }

  def dataTable(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc")
    )

    var db = ""
    val total = getResult(jobID).map {
      case Some(jsValue) =>
        val result = psiblast.parseResult(jsValue)
        db = result.db
        result.num_hits

    }
    val hits = getHitsByKeyWord(jobID, params)

    hits.flatMap { list =>
      total.map { total_ =>
        Ok(
          Json
            .toJson(Map("iTotalRecords" -> total_, "iTotalDisplayRecords" -> total_))
            .as[JsObject]
            .deepMerge(Json.obj("aaData" -> list.map(_.toDataTable(db)))))
      }
    }
  }

}
