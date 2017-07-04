package controllers

import javax.inject.Inject

import com.typesafe.config.ConfigFactory
import better.files._
import models.Constants

import scala.concurrent.ExecutionContext.Implicits.global
import models.database.results._
import modules.db.MongoStore
import play.api.libs.json.{ JsArray, JsObject, Json }
import play.api.mvc.{ Action, AnyContent, Controller }
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import scala.sys.process.Process

/**
  * Created by drau on 18.04.17.
  */
class HmmerController @Inject()(hmmer: Hmmer,
                                general: General,
                                aln: Alignment,
                                constants: Constants)(mongoStore: MongoStore,
                                                                                val reactiveMongoApi: ReactiveMongoApi)
    extends Controller
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
          val result        = hmmer.parseResult(jsValue)
          val accessionsStr = getAccessionsEval(result, eval.toDouble)
          val db            = result.db
          Process(retrieveFullSeq.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
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
    val json    = request.body.asJson.get
    val filename  = (json \ "filename").as[String]
    val numList = (json \ "checkboxes").as[List[Int]]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result        = hmmer.parseResult(jsValue)
          val accessionsStr = getAccessions(result, numList)
          val db            = result.db
          Process(retrieveFullSeq.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
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

  def getAccessions(result: HmmerResult, numList: Seq[Int]): String = {
    val fas = numList.map { num =>
      result.HSPS(num - 1).accession + " "
    }
    fas.mkString
  }
  def getAccessionsEval(result: HmmerResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.evalue < eval).map { _.accession + " " }
    fas.mkString
  }

  def alnEval(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val eval      = (json \ "evalue").as[String]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) => Ok(getAlnEval(hmmer.parseResult(jsValue), eval.toDouble))
      case _             => NotFound
    }
  }

  def aln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val numList = (json \ "checkboxes").as[List[Int]]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) => Ok(getAln(aln.parseAlignment((jsValue \ "alignment").as[JsArray]), numList))
      case _             => NotFound
    }
  }

  def getAlnEval(result: HmmerResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.evalue < eval).map { hit =>
      ">" + result.alignment(hit.num - 1).accession + "\n" + result.alignment(hit.num - 1).seq + "\n"
    }
    fas.mkString
  }

  def getAln(alignment: AlignmentResult, list: Seq[Int]): String = {
    val fas = list.map { num =>
      ">" + alignment.alignment(num - 1).accession + "\n" + alignment.alignment(num - 1).seq + "\n"
    }
    fas.mkString
  }

  def getHitsByKeyWord(jobID: String, params: DTParam): Future[List[HmmerHSP]] = {
    if (params.sSearch.isEmpty) {
      mongoStore.getResult(jobID).map {
        case Some(result) =>
          hmmer
            .hitsOrderBy(params, hmmer.parseResult(result).HSPS)
            .slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    } else {
      ???
    }
  }

  def loadHits(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json      = request.body.asJson.get
    val start     = (json \ "start").as[Int]
    val end       = (json \ "end").as[Int]
    val wrapped       = (json \ "wrapped").as[Boolean]
    mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = hmmer.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits = result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hmmer.hit(jobID, _, result.db, wrapped))
          Ok(hits.mkString)
        }

    }
  }

  /**
    * DataTables for job results
    */
  def dataTable(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc")
    )

    val hits = getHitsByKeyWord(jobID, params)
    var db   = ""
    val total = mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = hmmer.parseResult(jsValue)
        db = result.db
        result.num_hits

    }
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
