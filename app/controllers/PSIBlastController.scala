/**
  * Created by drau on 01.03.17.
  */
package controllers

import com.typesafe.config.ConfigFactory

import scala.sys.process._
import better.files._
import models.Constants
import models.database.results._
import play.api.mvc.{ Action, AnyContent, Controller }
import javax.inject.Inject

import modules.db.MongoStore
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import play.api.libs.json.{ JsArray, JsObject, Json }

import scala.concurrent.ExecutionContext.Implicits.global

class PSIBlastController @Inject()(
                                    psiblast: PSIBlast,
                                    general: General,
                                    alignment: Alignment
)(webJarAssets: WebJarAssets, mongoStore: MongoStore, val reactiveMongoApi: ReactiveMongoApi)
    extends Controller
    with Constants
    with Common {

  private val serverScripts   = ConfigFactory.load().getString("serverScripts")
  private val retrieveFullSeq = (serverScripts + "/retrieveFullSeq.sh").toFile

  def evalFull(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val filename  = (json \ "filename").as[String]
    val eval      = (json \ "evalue").as[String]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result        = psiblast.parseResult(jsValue)
          val accessionsStr = getAccessionsEval(result, eval.toDouble)
          val db            = result.db
          Process(retrieveFullSeq.pathAsString,
                  (jobPath + jobID).toFile.toJava,
                  "jobID"         -> jobID,
                  "accessionsStr" -> accessionsStr,
                  "filename"      -> filename,
                  "db"            -> db).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }

        case _ => NotFound
      }
    }
  }

  def full(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    println("called")
    val json       = request.body.asJson.get
    val numList    = (json \ "checkboxes").as[List[Int]]
    val filename   = (json \ "filename").as[String]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result        = psiblast.parseResult(jsValue)
          val accessionsStr = getAccessions(result, numList)
          val db            = result.db
          Process(retrieveFullSeq.pathAsString,
                  (jobPath + jobID).toFile.toJava,
                  "jobID"         -> jobID,
                  "accessionsStr" -> accessionsStr,
                  "filename"      -> filename,
                  "db"            -> db).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }

        case _ => NotFound
      }
    }
  }

  def alnEval(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json      = request.body.asJson.get
    val eval      = (json \ "evalue").as[String]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) => Ok(getAlnEval(psiblast.parseResult(jsValue), eval.toDouble))
      case _             => NotFound
    }
  }

  def aln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json      = request.body.asJson.get
    val numList   = (json \ "checkboxes").as[List[Int]]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) => Ok(getAln(alignment.parseAlignment((jsValue \ "alignment").as[JsArray]), numList))
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
      mongoStore.getResult(jobID).map {
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

  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json      = request.body.asJson.get
    val start     = (json \ "start").as[Int]
    val end       = (json \ "end").as[Int]
    val wrapped       = (json \ "wrapped").as[Boolean]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = psiblast.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits = result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.psiblast.hit(jobID, _, result.db, wrapped))
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
    val total = mongoStore.getResult(jobID).map {
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
            .deepMerge(Json.obj("aaData" -> list.map(_.toDataTable(db))))
        )
      }
    }
  }

}
