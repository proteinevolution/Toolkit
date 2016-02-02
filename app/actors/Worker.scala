package actors


import java.io
import java.io.PrintWriter

import actors.UserActor.{JobDone, PrepWDDone}
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import models._
import play.api.Logger
import scala.io.Source
import sys.process._


import scala.reflect.io.{Directory, File}


class Worker extends Actor with ActorLogging {

  import actors.Worker._

  val sep = File.separator

  // Variables the worker need to execute //TODO Inject Configuration
  val runscriptPath = "bioprogs/runscripts/"
  val jobPath = "development/"

  val subdirs = Array("/results", "/logs", "/params")

  val argumentPattern = "(\\$\\{[a-z]+\\}|#\\{[a-z]+\\}|@\\{[a-z]+\\})".r


  def receive = LoggingReceive {

    case WPrepare(sjob) =>

      Logger.info("[Worker](WPrepare) for job " + sjob.id)
      Logger.info("[Worker] Runscript path was " + runscriptPath)
      Logger.info("[Worker] Job path was " + jobPath)
      val rootPath = jobPath + sjob.uid + sep + sjob.id + sep

      // Make root Path and all subpaths
      Directory(rootPath).createDirectory(false, false)
      for(subdir <- subdirs) {

        Directory(rootPath + subdir).createDirectory(false, false)
      }

      // Write the parameters into the subdirectory:
      for( (paramName, value) <- sjob.params ) {

        File(rootPath + "/params/" + paramName).writeAll(value.toString)
      }


      val sourceRunscript = Source.fromFile(runscriptPath + sjob.toolname + ".sh")
      val targetRunscript = new PrintWriter(rootPath + sjob.toolname + ".sh")

      for(line <- sourceRunscript.getLines) {

        targetRunscript.println(argumentPattern.replaceAllIn(line, { rm =>

          val s = rm.toString()
          val value = s.substring(2, s.length - 1)

          s(0) match {

            case '#' =>  "params/" + value
            case '$' => sjob.params.get(value).get.toString
            case '@' => "results/" + value
          }
        }))
      }
      sourceRunscript.close()
      targetRunscript.close()

      // Set the execution right
      ("chmod u+x " + rootPath + sjob.toolname + ".sh").!

      sender() ! PrepWDDone(sjob.stripParams())


    case WStart(job) =>

      Logger.info("[Worker](WStart) for job " + job.id)
      val rootPath = jobPath + job.uid + sep + job.id + sep
      val result = Process("./" + job.toolname + ".sh", new io.File(rootPath)).!

      // If script has run successfully, send back to sender
      job.state = if(result == 0) models.Done else models.Error

      sender() ! JobDone(job)
  }
}


/**
  *
  * Created by lukas on 1/16/16.
  */


object Worker {

  case class WPrepare(job : SuppliedJob)
  case class WStart(job: Job)
}
