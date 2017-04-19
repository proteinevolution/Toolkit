package controllers


import play.api.mvc.{Action, AnyContent, Controller}
import java.nio.file.attribute.PosixFilePermission
import com.typesafe.config.ConfigFactory
import scala.sys.process._
import better.files._
import models.Constants
import models.database.results.{PSIBlast, PSIBlastResult}
import play.api.mvc.{Action, AnyContent, Controller}
import javax.inject.Inject
import play.modules.reactivemongo.ReactiveMongoApi
import scala.concurrent.Future
import modules.CommonModule
import scala.concurrent.ExecutionContext.Implicits.global


/**
  * Created by drau on 01.03.17.
  */
class PSIBlastController @Inject()(webJarAssets : WebJarAssets, val reactiveMongoApi : ReactiveMongoApi) extends Controller with Constants with CommonModule{
  private val serverScripts = ConfigFactory.load().getString("serverScripts")
  private val retrieveFullSeq = (serverScripts + "/retrieveFullSeq.sh").toFile
  private val alignSeqs = (serverScripts + "/align.sh").toFile
  private final val filePermissions = Set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)

  retrieveFullSeq.setPermissions(filePermissions)


  def retrieveFullSeqs(jobID : String) : Action[AnyContent] = Action.async { implicit request =>

    if(!retrieveFullSeq.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${retrieveFullSeq.name} is not executable.")
    }
    else {
      Future.successful{
        val json =  request.body.asJson.get
        val db = (json \ "db").get.as[String]
        val accessionsStr = (json \ "accessionsStr").get.as[String]

        Process(retrieveFullSeq.pathAsString, (jobPath + jobID).toFile.toJava, "jobID" -> jobID, "accessionsStr" -> accessionsStr, db -> "db").run().exitValue() match {

          case 0 => Ok
          case _ => BadRequest
        }
      }
    }
  }

  def getAlignment(jobID : String) : Action[AnyContent] = Action.async { implicit request =>

    if(!alignSeqs.isExecutable) {
      Future.successful(BadRequest)
      throw FileException(s"File ${alignSeqs.name} is not executable.")
    }
    else {
      Future.successful{
        val json =  request.body.asJson.get
        val start = (json \ "start").get.as[String]
        val end = (json \ "end").get.as[String]
        val accessionsStr = (json \ "accessionsStr").get.as[String]

        Process(alignSeqs.pathAsString, (jobPath + jobID).toFile.toJava, "jobID" -> jobID, "accessionsStr" -> accessionsStr, start -> "start" , end -> "end").run().exitValue() match {

          case 0 => Ok
          case _ => BadRequest
        }
      }
    }
  }


  def evalPSIBlast(jobID: String, eval: String): Action[AnyContent] = Action.async { implicit request =>
    getResult(jobID).map {
      case Some(jsValue) => Ok(getFasEval(PSIBlast.parsePSIBlastResult(jsValue), eval.toDouble))
      case _=> NotFound
    }
  }

  def fasPSIBlast(jobID : String, numList: Seq[Int]): Action[AnyContent] = Action.async { implicit request =>
    getResult(jobID).map {
      case Some(jsValue) => Ok(getFas(PSIBlast.parsePSIBlastResult(jsValue), numList))
      case _ => NotFound
    }
  }

  def getFasEval(result : PSIBlastResult, eval : Double): String = {
    val fas = result.HSPS.map { hit =>
      if(hit.evalue < eval){
        ">" + hit.accession + "\n" + hit.hit_seq + "\n"
      }
    }
    fas.mkString
  }

  def getFas(result : PSIBlastResult, list: Seq[Int]): String = {
    val fas = list.map { num =>
      ">" + result.HSPS(num - 1).accession + "\n" + result.HSPS(num - 1).hit_seq + "\n"
    }
    fas.mkString
  }




  // Exceptions
  case class FileException(message : String) extends Exception(message)
}
