package actors

import java.io.File

import akka.actor.{ActorLogging, Actor}
import helpers.FileAccess
import play.api.Play._
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
    case Start(jobID) =>

      // path of working directory
      val path = s"${current.configuration.getString("job_path").get}${File.separator}$jobID"

      // read the toolname file to get the tool configuration
      val toolname = FileAccess.readFile(path + "/" + "toolname")




      //////////////////////////////////////////////////////////////

      // Job execution is done
      //sender ! JobDone(userActor, toolname, details, jobID)
  }
}
