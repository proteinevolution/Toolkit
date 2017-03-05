package controllers

import javax.inject.{Inject, Singleton}

import java.nio.file.attribute.PosixFilePermission

import com.typesafe.config.ConfigFactory
import play.api.Logger
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future
import scala.sys.process._
import better.files._
import models.Constants


/**
  * Created by drau on 01.03.17.
  */
class HHpredController @Inject()(webJarAssets : WebJarAssets) extends Controller with Constants {
  private val serverScripts = ConfigFactory.load().getString("serverScripts")
  private val templateAlignmentScript = (serverScripts + "/templateAlignment.sh").toFile
  private final val filePermissions = Set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)

  templateAlignmentScript.setPermissions(filePermissions)


  def show3DStructure(accession: String) = Action { implicit request =>
    Ok(views.html.jobs.resultpanels.structure(accession, webJarAssets))
  }

  def runScript(jobID: String, accession: String) = Action.async {
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
  // Exceptions
  case class FileException(message : String) extends Exception(message)
}
