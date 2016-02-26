package actors


import java.io
import java.io.PrintWriter
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import models.graph.Ready
import models.jobs._
import play.api.Logger
import scala.concurrent.Await
import scala.io.Source
import sys.process._
import scala.concurrent.duration._


import scala.reflect.io.{Directory, File}



object Worker {
  // Preparation of the Job
  case class WPrepare(job : UserJob, params : Map[String, String])
  // Starting the job
  case class WStart(job: UserJob)

  // Worker was asked to read parameters of the job and to put them into a Map
  case class WRead(job : UserJob)
}

class Worker @Inject() (jobDB : models.database.Jobs) extends Actor with ActorLogging {

  import actors.Worker._

  val sep = File.separator

  // Variables the worker need to execute //TODO Inject Configuration
  val runscriptPath = s"bioprogs${sep}runscripts$sep"
  val jobPath = s"development$sep"
  val subdirs = Array("/results", "/logs", "/params", "/specific")

  val argumentPattern = "(\\$\\{[a-z]+\\}|#\\{[a-z]+\\}|@\\{[a-z]+\\}|\\?\\{.+\\})".r


  def receive = LoggingReceive {

    case WPrepare(userJob, params) =>

      Logger.info("[Worker](WPrepare) for job " + userJob.job_id)
      Logger.info("[Worker] Runscript path was " + runscriptPath)
      Logger.info("[Worker] Job path was " + jobPath)


      // Worker will wait until it knows the Main ID
      val main_id = Await.result(jobDB.userJobMapping.get(userJob.user_id -> userJob.job_id).get, Duration.Inf)
      val rootPath = jobPath + sep + main_id.toString + sep

      // Make root Path and all subpaths
      Directory(rootPath).createDirectory(false, false)
      for(subdir <- subdirs) {

        Directory(rootPath + subdir).createDirectory(false, false)
      }
      Logger.info("All subdirectories were created successfully")


      // Write the parameters into the subdirectory:
      for( (paramName, value) <- params ) {

        File(s"$rootPath${sep}params$sep$paramName").writeAll(value.toString)
        userJob.changeInFileState(paramName, Ready)
      }
      Logger.info("All params were written to the job_directory successfully")

      val sourceRunscript = Source.fromFile(runscriptPath + userJob.toolname + ".sh")
      val targetRunscript = new PrintWriter(rootPath + userJob.toolname + ".sh")


      // Translate the Runscript template to an actual executable script // TODO We should apply some abstraction here
      for(line <- sourceRunscript.getLines) {

        targetRunscript.println(argumentPattern.replaceAllIn(line, { rm =>

          val s = rm.toString()
          val value = s.substring(2, s.length - 1)

          s(0) match {

            case '#' =>  "params/" + value
            case '$' => params.get(value).get.toString
            case '@' => "results/" + value
            case '?' =>

              val splt = value.split("(\\||:)")
              if(params.get(splt(0)).get.asInstanceOf[Boolean]) splt(1) else {

                if(splt.length == 2) "" else splt(2)
              }
          }
        }))
      }
      ("chmod u+x " + rootPath + userJob.toolname + ".sh").!    // TODO Is there a neater way to change the permission?
      sourceRunscript.close()
      targetRunscript.close()
      userJob.changeState(Prepared)


    case WRead(userJob) =>

      val main_id = Await.result(jobDB.userJobMapping.get(userJob.user_id -> userJob.job_id).get, Duration.Inf)
      val paramPath = jobPath + main_id + sep + "params/"

      val files = new java.io.File(paramPath).listFiles

      val res : Map[String, String] = files.map { file =>

        file.getName -> scala.io.Source.fromFile(file.getAbsolutePath).mkString
      }.toMap
      Logger.info("Worker reached")
      sender() ! res





    case WStart(userJob) =>

      Logger.info("[Worker](WStart) for job " + userJob.job_id)
      val main_id = Await.result(jobDB.userJobMapping.get(userJob.user_id -> userJob.job_id).get, Duration.Inf)
      val rootPath = jobPath + main_id + sep

      // Assumption : The Root path contains a prepared shellscript that bears the toolname + sh suffix
      val result = Process("./" + userJob.toolname + ".sh", new io.File(rootPath)).!

      // Change state of job depending on the RUnscript execution
      userJob.changeState(if(result == 0) Done else Error)
  }
}
