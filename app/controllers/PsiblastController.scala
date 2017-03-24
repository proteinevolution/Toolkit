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
class PsiblastController @Inject()(webJarAssets : WebJarAssets) extends Controller with Constants {
  private val serverScripts = ConfigFactory.load().getString("serverScripts")
  private val retrieveFullSeq = (serverScripts + "/retrieveFullSeq.sh").toFile
  private val alignSeqs = (serverScripts + "/align.sh").toFile
  private final val filePermissions = Set(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)

  retrieveFullSeq.setPermissions(filePermissions)


  def retrieveSeqs(jobID : String) = Action.async { implicit request =>

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

  def getAlignment(jobID : String) = Action.async { implicit request =>

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
  // Exceptions
  case class FileException(message : String) extends Exception(message)
}
