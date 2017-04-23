package controllers

import javax.inject.{Inject}
import java.nio.file.attribute.PosixFilePermission

import com.typesafe.config.ConfigFactory
import play.api.mvc.{Action, AnyContent, Controller}
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import scala.sys.process._
import better.files._
import models.Constants
import models.database.results.{HHPred, HHPredHSP}
import play.modules.reactivemongo.ReactiveMongoApi
import modules.CommonModule
import play.api.libs.json.{JsArray, JsObject, Json}


/**
  * Created by drau on 01.03.17.
  */
class HHpredController @Inject()(hhpred: HHPred, val reactiveMongoApi : ReactiveMongoApi)(webJarAssets : WebJarAssets) extends Controller with Constants with CommonModule {
  private val serverScripts = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignment.sh").toFile
  private final val filePermissions = Set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)

  templateAlignmentScript.setPermissions(filePermissions)


  def show3DStructure(accession: String) : Action[AnyContent] = Action { implicit request =>
    Ok(views.html.jobs.resultpanels.structure(accession, webJarAssets))
  }

  def runScript(jobID: String, accession: String) : Action[AnyContent] = Action.async {
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

  def getHitsByKeyWord(jobID: String, params: DTParam) : Future[List[HHPredHSP]] = {
    if(params.sSearch.isEmpty){
      getResult(jobID).map {
        case Some(result) => hhpred.parseResult(result).HSPS.slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
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
        val result = hhpred.parseResult(jsValue)
        db = result.db
        result.num_hits
      }
    }
    val hits = getHitsByKeyWord(jobID, params)

    hhpred.hitsOrderBy(params, hits).flatMap { list =>
      total.map { total_ =>
        Ok(Json.toJson(Map("iTotalRecords" -> total_, "iTotalDisplayRecords" -> total_))
          .as[JsObject].deepMerge(Json.obj("aaData" -> list.map(_.toDataTable(db)))))
      }
    }
  }
  // Exceptions
  case class FileException(message : String) extends Exception(message)
}
