package controllers

import javax.inject.{Inject, Singleton}
import java.nio.file.attribute.PosixFilePermission

import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.Future
import scala.sys.process._
import better.files._
import models.Constants
import models.database.results.{General, HHBlits, HHBlitsHSP}
import modules.CommonModule
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by drau on 01.03.17.
  */
class HHblitsController @Inject()(webJarAssets : WebJarAssets, val reactiveMongoApi : ReactiveMongoApi, hhblits: HHBlits, general : General) extends Controller with Constants with CommonModule{
  private val serverScripts = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignmentHHblits.sh").toFile
  private val retrieveFullSeqScript = (serverScripts + "/retrieveFullSeqHHblits.sh").toFile
  private final val filePermissions = Set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)

  templateAlignmentScript.setPermissions(filePermissions)
  retrieveFullSeqScript.setPermissions(filePermissions)


  def show3DStructure(accession: String) : Action[AnyContent] = Action { implicit request =>
    Ok(views.html.jobs.resultpanels.structure(accession, webJarAssets))
  }

  def retrieveTemplateAlignment(jobID: String, accession: String) : Action[AnyContent] = Action.async {
    if(jobID.isEmpty || accession.isEmpty){
      Logger.info("either job or accession is empty")
    }
    if(!templateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${templateAlignmentScript.name} is not executable.")
    }
    else {
      Future.successful{
        Process(templateAlignmentScript.pathAsString, (jobPath + jobID).toFile.toJava, "jobID" -> jobID, "accession" -> accession).run().exitValue() match {
          case 0 => Ok
          case _ => BadRequest
        }
      }
    }
  }

  def retrieveFullSeq(jobID: String) : Action[AnyContent] = Action.async { implicit request =>
    if(!templateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeqScript.name} is not executable.")
    }
    else {
      Future.successful{
        val json =  request.body.asJson.get
        val accessionsStr = (json \ "accessionsStr").get.as[String]

        Process(retrieveFullSeqScript.pathAsString, (jobPath + jobID).toFile.toJava, "jobID" -> jobID, "accessionsStr" -> accessionsStr).run().exitValue() match {

          case 0 => Ok
          case _ => BadRequest
        }
      }
    }
  }

  def getHitsByKeyWord(jobID: String, params: DTParam) : Future[List[HHBlitsHSP]] = {
    if(params.sSearch.isEmpty){
      getResult(jobID).map {
        case Some(result) => hhblits.parseResult(result).HSPS.slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    }else{
      ???
    }
    //case false => (for (s <- getHits if (title.startsWith(params.sSearch))) yield (s)).list
  }
  def dataTable(jobID : String) : Action[AnyContent] = Action.async { implicit request =>
    val params = DTParam(
      request.getQueryString("sSearch").getOrElse(""),
      request.getQueryString("iDisplayStart").getOrElse("0").toInt,
      request.getQueryString("iDisplayLength").getOrElse("100").toInt,
      request.getQueryString("iSortCol_0").getOrElse("1").toInt,
      request.getQueryString("sSortDir_0").getOrElse("asc"))

    var db = ""
    val total = getResult(jobID).map {
      case Some(jsValue) => {
        val result = hhblits.parseResult(jsValue)
        db = result.db
        result.num_hits
      }
    }
    val hits = getHitsByKeyWord(jobID, params)

    hhblits.hitsOrderBy(params, hits).flatMap { list =>
      total.map { total_ =>
        Ok(Json.toJson(Map("iTotalRecords" -> total_, "iTotalDisplayRecords" -> total_))
          .as[JsObject].deepMerge(Json.obj("aaData" -> list.map(_.toDataTable(db)))))
      }
    }
  }

  // Exceptions
  case class FileException(message : String) extends Exception(message)
}
