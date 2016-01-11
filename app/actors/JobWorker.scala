package actors

import java.io.File

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive
import helpers.FileAccess
import models._
import play.api.Play._
import language.postfixOps
import scala.sys.process._

/**
  * Class Intended to take care for the execution of one particular Job
  *
  * Created by lukas on 1/8/16.
  */
class JobWorker extends Actor with ActorLogging {

  def receive = LoggingReceive {

    /**
      * Tell the worker to start the new Job with the details
      */
    case Start(jobID) =>

      // path of working directory
      val path = s"${current.configuration.getString("job_path").get}${File.separator}$jobID"

      // read the toolname file to get the tool configuration
      val toolname = FileAccess.readFile(path + "/" + "toolname")

      // Assemble Bash script

      // parse the exec String and replace the occurrences =of parameters accordingly
      val call =
      models.Values.modelMap.get(toolname).get.exec map { elem =>
        elem match {

          case KeyValuePair(key, value, prefix, sep) =>

            value match {
              // Now distinguish between FileParam and StringParam
              // TODO default Values are currently not supported
              case FileParam(name) => prefix + key + sep + path + "/" + name

              case StringParam(name)  =>

                prefix + key + sep +  FileAccess.readFile(path + "/" + name)

              case ConstParam(name) => prefix + key + sep + name

              case ResFileParam(name) => prefix + key + sep + path + "/" + name
            }
          case Interpreter(name) => name

          case HelperScript(name) => "app/helpers/" + name
        }
      } mkString " "

      // execute call and fetch exit code
      //val exit_code = call.!
      //print(exit_code)





    //////////////////////////////////////////////////////////////

      // Job execution is done
      //sender ! JobDone(userActor, toolname, details, jobID)
  }
}
