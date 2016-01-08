package actors

import akka.actor.{ActorLogging, Actor}
import play.api.Logger
import helper.FileAccess
import play.api.Play.current
import java.io.File
import language.postfixOps

/**
  * Class Intended to take care for the execution of one particular Job
  *
  * Created by lukas on 1/8/16.
  */
class JobWorker extends Actor with ActorLogging {

  def receive = {

    /**
      * Tell the worker to start the new Job with the details
      */
    case Start(toolname, details, jobID, userActor) =>

        log.info("Worker has been started")
      // We need to prepare a working directory for the job with all the input files ready
      // load job directory
      val path = s"${current.configuration.getString("job_path").get}${File.separator}$jobID"
      Logger.info(path)
      Logger.info(toolname)
      Logger.info(details)
      FileAccess.mkdir(path)
      //////////////////////////////////////////////////////////////

      // Parse the Get String as Scala Map
      val res = details.split('&') map { str =>
        val pair = str.split('=')
        pair(0) -> pair(1)
      } toMap

      Logger.info(res.toString())


      Thread sleep 4000

      // Job execution is done
      sender ! JobDone(userActor, toolname, details, jobID)
  }
}
