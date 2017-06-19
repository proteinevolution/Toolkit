package controllers

import javax.inject.{ Inject, Singleton }

import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.mvc.{ Action, AnyContent, Controller }

import scala.concurrent.Future
import scala.sys.process._
import better.files._
import models.Constants
import models.database.results.{ General, HHBlits, HHBlitsHSP, HHBlitsResult }
import modules.db.MongoStore
import play.api.libs.json.{ JsArray, JsObject, Json }
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by drau on 01.03.17.
  */
class HHblitsController @Inject()(webJarAssets: WebJarAssets,
                                  mongoStore: MongoStore,
                                  val reactiveMongoApi: ReactiveMongoApi,
                                  hhblits: HHBlits,
                                  general: General)
    extends Controller
    with Constants
    with Common {
  private val serverScripts           = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignmentHHblits.sh").toFile
  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile
  private val retrieveFullSeq         = (serverScripts + "/retrieveFullSeqHHblits.sh").toFile

  def show3DStructure(accession: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.jobs.resultpanels.structure(accession, webJarAssets))
  }

  def retrieveTemplateAlignment(jobID: String, accession: String): Action[AnyContent] = Action.async {
    if (jobID.isEmpty || accession.isEmpty) {
      Logger.info("either job or accession is empty")
    }
    if (!templateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${templateAlignmentScript.name} is not executable.")
    } else {
      Future.successful {
        Process(templateAlignmentScript.pathAsString,
                (jobPath + jobID).toFile.toJava,
                "jobID"     -> jobID,
                "accession" -> accession).run().exitValue() match {
          case 0 => Ok
          case _ => BadRequest
        }
      }
    }
  }
  def evalFull(jobID: String, eval: String, filename: String): Action[AnyContent] = Action.async { implicit request =>
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result        = hhblits.parseResult(jsValue)
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
  def full(jobID: String, filename: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val numList = (json \ "checkboxes").as[List[Int]]
    if (!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result        = hhblits.parseResult(jsValue)
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

  def alnEval(jobID: String, eval: String, filename: String): Action[AnyContent] = Action.async { implicit request =>
    if (!generateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${generateAlignmentScript.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result     = hhblits.parseResult(jsValue)
          val numListStr = getNumListEval(result, eval.toDouble)
          Process(generateAlignmentScript.pathAsString,
                  (jobPath + jobID).toFile.toJava,
                  "jobID"   -> jobID,
                  "filename"-> filename,
                  "numList" -> numListStr).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }

        case _ => NotFound
      }
    }
  }

  def aln(jobID: String, filename: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val numList = (json \ "checkboxes").as[List[Int]]
    if (!generateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${generateAlignmentScript.name} is not executable.")
    } else {
      val numListStr = numList.mkString(" ")
      Process(generateAlignmentScript.pathAsString,
              (jobPath + jobID).toFile.toJava,
              "jobID"   -> jobID,
              "filename"-> filename,
              "numList" -> numListStr).run().exitValue() match {
        case 0 => Future.successful(Ok)
        case _ => Future.successful(BadRequest)
      }
    }
  }

  def getNumListEval(result: HHBlitsResult, eval: Double): String = {
    val numList = result.HSPS.filter(_.info.evalue < eval).map { _.num }
    numList.mkString(" ")
  }

  def getAccessions(result: HHBlitsResult, numList: Seq[Int]): String = {
    val fas = numList.map { num =>
      result.HSPS(num - 1).template.accession + " "
    }
    fas.mkString
  }
  def getAccessionsEval(result: HHBlitsResult, eval: Double): String = {
    val fas = result.HSPS.filter(_.info.evalue < eval).map { _.template.accession + " " }
    fas.mkString
  }

  def getHitsByKeyWord(jobID: String, params: DTParam): Future[List[HHBlitsHSP]] = {
    if (params.sSearch.isEmpty) {
      mongoStore.getResult(jobID).map {
        case Some(result) =>
          hhblits
            .hitsOrderBy(params, hhblits.parseResult(result).HSPS)
            .slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    } else {
      ???
    }
    //case false => (for (s <- getHits if (title.startsWith(params.sSearch))) yield (s)).list
  }
  def loadHits(jobID: String, start: Int, end: Int): Action[AnyContent] = Action.async { implicit request =>
    mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = hhblits.parseResult(jsValue)
        if (end > result.num_hits || start > result.num_hits) {
          BadRequest
        } else {
          val hits = result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hhblits.hit(jobID, _))
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
        val result = hhblits.parseResult(jsValue)
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
