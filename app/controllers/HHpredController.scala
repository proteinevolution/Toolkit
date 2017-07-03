package controllers

import javax.inject.Inject
import java.nio.file.attribute.PosixFilePermission

import com.typesafe.config.ConfigFactory
import play.api.mvc.{ Action, AnyContent, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.sys.process._
import better.files._
import models.Constants
import models.database.results.{ HHPred, HHPredHSP, HHPredResult }
import modules.db.MongoStore
import play.modules.reactivemongo.ReactiveMongoApi
import play.api.libs.json.{ JsArray, JsObject, Json }

/**
  * Created by drau on 01.03.17.
  */
class HHpredController @Inject()(hhpred: HHPred, mongoStore: MongoStore, val reactiveMongoApi: ReactiveMongoApi, constants: Constants)(
    webJarAssets: WebJarAssets
) extends Controller
    with Common {
  private val serverScripts           = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignment.sh").toFile
  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile

  def show3DStructure(accession: String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.jobs.resultpanels.structure(accession, webJarAssets))
  }

  def runScript(jobID: String, accession: String): Action[AnyContent] = Action.async { implicit request =>
    if (!templateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${templateAlignmentScript.name} is not executable.")
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
  def alnEval(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val filename  = (json \ "fileName").as[String]
    val eval      = (json \ "evalue").as[String]
    if (!generateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${generateAlignmentScript.name} is not executable.")
    } else {
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result     = hhpred.parseResult(jsValue)
          val numListStr = getNumListEval(result, eval.toDouble)
          Process(generateAlignmentScript.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
                  "jobID"   -> jobID,
                  "filename" -> filename,
                  "numList" -> numListStr).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }

        case _ => NotFound
      }
    }
  }

  def aln(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    val json    = request.body.asJson.get
    val filename  = (json \ "fileName").as[String]
    val numList = (json \ "checkboxes").as[List[Int]]
    if (!generateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${generateAlignmentScript.name} is not executable.")
    } else {
      val numListStr = numList.mkString(" ")
      Process(generateAlignmentScript.pathAsString,
              (constants.jobPath + jobID).toFile.toJava,
              "jobID"   -> jobID,
              "filename"-> filename,
              "numList" -> numListStr).run().exitValue() match {
        case 0 => Future.successful(Ok)
        case _ => Future.successful(BadRequest)
      }
    }
  }

  def getNumListEval(result: HHPredResult, eval: Double): String = {
    val numList = result.HSPS.filter(_.info.evalue < eval).map { _.num }
    numList.mkString(" ")
  }

  def getHitsByKeyWord(jobID: String, params: DTParam): Future[List[HHPredHSP]] = {
    if (params.sSearch.isEmpty) {
      mongoStore.getResult(jobID).map {
        case Some(result) =>
          hhpred
            .hitsOrderBy(params, hhpred.parseResult(result).HSPS)
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
    val isColor       = (json \ "isColor").as[Boolean]
      mongoStore.getResult(jobID).map {
        case Some(jsValue) =>
          val result = hhpred.parseResult(jsValue)
          if (end > result.num_hits || start > result.num_hits) {
            BadRequest
          } else {
            val hits = result.HSPS.slice(start, end).map(views.html.jobs.resultpanels.hhpred.hit(jobID, _, isColor))
            Ok(hits.mkString)
          }
      }
  }

  def dataTable(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    var db = ""
    val total = mongoStore.getResult(jobID).map {
      case Some(jsValue) =>
        val result = hhpred.parseResult(jsValue)
        db = result.db
        result.num_hits

    }
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc")
    )

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
