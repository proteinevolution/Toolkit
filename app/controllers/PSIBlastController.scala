/**
  * Created by drau on 01.03.17.
  */
package controllers


import play.api.mvc.{Action, AnyContent, Controller}
import java.nio.file.attribute.PosixFilePermission

import com.typesafe.config.ConfigFactory

import scala.sys.process._
import better.files._
import models.Constants
import models.database.results._
import play.api.mvc.{Action, AnyContent, Controller}
import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.Future
import modules.CommonModule
import play.api.libs.json.JsArray

import scala.concurrent.ExecutionContext.Implicits.global


class PSIBlastController @Inject() (psiblast: PSIBlast, general : General)(webJarAssets : WebJarAssets, val reactiveMongoApi : ReactiveMongoApi) extends Controller with Constants with CommonModule{
  private val serverScripts = ConfigFactory.load().getString("serverScripts")
  private val retrieveFullSeq = (serverScripts + "/retrieveFullSeq.sh").toFile
  private final val filePermissions = Set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)

  retrieveFullSeq.setPermissions(filePermissions)
  def evalFull(jobID : String, eval: String) : Action[AnyContent] = Action.async { implicit request =>

    if(!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      getResult(jobID).map {
        case Some(jsValue) => {
          val result = psiblast.parseResult(jsValue)
          val accessionsStr = getAccessionsEval(result, eval.toDouble)
          val db = result.db
          Process(retrieveFullSeq.pathAsString, (jobPath + jobID).toFile.toJava, "jobID" -> jobID, "accessionsStr" -> accessionsStr, "db" -> db).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }
        }
        case _=> NotFound
      }
    }
  }

  def full(jobID : String, numList: Seq[Int]) : Action[AnyContent] = Action.async { implicit request =>

    if(!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    } else {
      getResult(jobID).map {
      case Some(jsValue) => {
        val result = psiblast.parseResult(jsValue)
        val accessionsStr = getAccessions(result, numList)
        val db = result.db
          Process(retrieveFullSeq.pathAsString, (jobPath + jobID).toFile.toJava, "jobID" -> jobID, "accessionsStr" -> accessionsStr, "db" -> db).run().exitValue() match {
            case 0 => Ok
            case _ => BadRequest
        }
      }
      case _=> NotFound
      }
    }
  }


  def alnEval(jobID: String, eval: String): Action[AnyContent] = Action.async { implicit request =>
    getResult(jobID).map {
      case Some(jsValue) => Ok(getAlnEval(psiblast.parseResult(jsValue), eval.toDouble))
      case _=> NotFound
    }
  }

  def aln(jobID : String, numList: Seq[Int]): Action[AnyContent] = Action.async { implicit request =>
    getResult(jobID).map {
      case Some(jsValue) => Ok(getAln(general.parseAlignment((jsValue \ "alignment").as[JsArray]), numList))
      case _ => NotFound
    }
  }

  def getAlnEval(result : PSIBlastResult,  eval : Double): String = {
    val fas = result.HSPS.map { hit =>
      if(hit.evalue < eval){
        ">" + result.alignment(hit.num -1).accession + "\n" + result.alignment(hit.num-1).seq + "\n"
      }
    }
    fas.mkString
  }

  def getAln(alignment : Alignment, numList: Seq[Int]): String = {
    val fas = numList.map { num =>
      ">" + alignment.alignment(num - 1).accession + "\n" + alignment.alignment(num -1 ).seq + "\n"
    }
    fas.mkString
  }

  def getAccessions(result : PSIBlastResult, numList : Seq[Int]) : String = {
    val fas = numList.map { num =>
      result.HSPS(num - 1).accession + " "
    }
    fas.mkString
  }
  def getAccessionsEval(result : PSIBlastResult, eval: Double) : String = {
    val fas = result.HSPS.map { hit =>
      if(hit.evalue < eval){
        hit.accession + " "
      }
    }
    fas.mkString
  }



  def getHitsByKeyWord(jobID: String, params: DTParam) : Future[List[PSIBlastHSP]] = {
    if(params.sSearch.isEmpty){
      getResult(jobID).map {
        case Some(result) => psiblast.parseResult(result).HSPS.slice(params.iDisplayStart, params.iDisplayStart + params.iDisplayLength)
      }
    }else{
      ???
    }
    //case false => (for (s <- getHits if (title.startsWith(params.sSearch))) yield (s)).list
  }
  // Exceptions
  case class FileException(message : String) extends Exception(message)
}
