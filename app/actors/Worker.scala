package actors


import java.io
import java.io.PrintWriter
import java.nio.file.{Files, Paths}
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import models.graph.{Ports, PortWithFormat, Ready}
import models.jobs._
import play.api.Logger
import scala.io.Source
import sys.process._


import scala.reflect.io.{Directory, File}



object Worker {
  // Preparation of the Job
  case class WPrepare(job : UserJob, params : Map[String, String])
  // Starting the job
  case class WStart(job: UserJob)

  case class WDelete(job : UserJob)

  // Worker was asked to read parameters of the job and to put them into a Map
  case class WRead(job : UserJob)


  case class WConvert(parentUserJob : UserJob, childUserJob : UserJob, links : Seq[Link])

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


      val main_id = jobDB.userJobMapping(userJob.user_id -> userJob.job_id)
      val rootPath = jobPath + sep + main_id.toString + sep



      ///
      ///  Step 1: Make the working directory of the job with all subdirectories
      ///
      if(!Files.exists(Paths.get(rootPath))) {
        Directory(rootPath).createDirectory(false, false)
        for (subdir <- subdirs) {

          Directory(rootPath + subdir).createDirectory(false, false)
        }
      }

      // TODO Should be moved to WStart to allow for partial preparation

      ///
      ///  Step 2: Get the runscript of the appropriate tool and replace template placeholders with
      ///  input parameters
      val sourceRunscript = Source.fromFile(runscriptPath + userJob.toolname + ".sh")
      val targetRunscript = new PrintWriter(rootPath + userJob.toolname + ".sh")

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
      Logger.info("Set file permission of: " + rootPath + userJob.toolname + ".sh")
      ("chmod u+x " + rootPath + userJob.toolname + ".sh").!    // TODO Is there a neater way to change the permission?
      sourceRunscript.close()
      targetRunscript.close()


      ///
      ///  Step 3: Write the parameters into the directory, this will also change the state
      ///          of the job as a side effect.
      for( (paramName, value) <- params ) {

        if(paramName != "jobid") {
          File(s"$rootPath${sep}params$sep$paramName").writeAll(value.toString)
          userJob.changeInFileState(paramName, Ready)
        }
      }
      Logger.info("All params were written to the job_directory successfully")


    case WRead(userJob) =>

      val main_id = jobDB.userJobMapping(userJob.user_id -> userJob.job_id)
      val paramPath = jobPath + main_id + sep + "params/"

      val files = new java.io.File(paramPath).listFiles

      val res : Map[String, String] = files.map { file =>

        file.getName -> scala.io.Source.fromFile(file.getAbsolutePath).mkString
      }.toMap

      sender() ! res


    case WDelete(userJob) =>

      val main_id = jobDB.delete(userJob.user_id, userJob.job_id)
      val rootPath = jobPath + main_id + sep
      Directory(rootPath).deleteRecursively()


    case WConvert(parentUserJob, childUserJob, links) =>

      val main_id = jobDB.userJobMapping(childUserJob.user_id -> childUserJob.job_id)
      val rootPath = jobPath + sep + main_id.toString + sep


      if(!Files.exists(Paths.get(rootPath))) {

        Directory(rootPath).createDirectory(false, false)
        for (subdir <- subdirs) {

          Directory(rootPath + subdir).createDirectory(false, false)
        }
      }






      Logger.info("Worker was asked to convert Jobs")
      for(link <- links) {

        val x = parentUserJob.tool.outports(link.out)
        val y = childUserJob.tool.inports(link.in)


      }










    case WStart(userJob) =>

      Logger.info("[Worker](WStart) for job " + userJob.job_id)
      val main_id = jobDB.userJobMapping(userJob.user_id -> userJob.job_id)
      val rootPath = jobPath + main_id + sep

      // Assumption : The Root path contains a prepared shellscript that bears the toolname + sh suffix

      // TODO Maybe we can use the Process builder in a more clever way

      val result = Process("./" + userJob.toolname + ".sh", new io.File(rootPath)).!

      // Change state of job depending on the RUnscript execution
      // TODO Add more error handling here
      userJob.changeState(if(result == 0) Done else Error)
  }
}
