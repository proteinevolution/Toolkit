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


/**
  * Created by drau on 01.03.17.
  */
class HHblitsController @Inject()(webJarAssets : WebJarAssets) extends Controller with Constants {
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


  // Exceptions
  case class FileException(message : String) extends Exception(message)
}
