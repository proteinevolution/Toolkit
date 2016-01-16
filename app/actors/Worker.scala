package actors

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import helpers.FileAccess
import models._
import play.api.Play.current
import java.io.File
import play.api.Logger
import scala.sys.process._


/**
  * Created by lukas on 1/16/16.
  */
class Worker extends Actor with ActorLogging {

  val TOOLNAME = "TOOLNAME"
  val UID = "UID"


  def receive = LoggingReceive {

    case Prepare(spec, jobID, toolname, uid) =>

      Logger.info("Worker starts to prepare working directory for jobID " + jobID + "\n")

      // create the Working Directory
      val path = s"${current.configuration.getString("job_path").get}${File.separator}$jobID"
      FileAccess.mkdir(path)

      for ((key , value) <- spec) {

        FileAccess.mkfile(path + "/" + key, value.toString)
      }
      FileAccess.mkfile(path + "/" + TOOLNAME, toolname)
      FileAccess.mkfile(path + "/" + UID, uid)

      sender ! PrepWDDone(jobID)


    case Start(jobID) =>

      Logger.info("Worker was told to start job" + jobID)

      val path = s"${current.configuration.getString("job_path").get}${File.separator}$jobID"

      // read the toolname file to get the tool configuration
      val toolname = FileAccess.readFile(path + "/" + TOOLNAME)

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

      Logger.info("System call was " + call)

      // TODO This might take a while
      val exit_code = call.!
      Logger.info("Exit code was " + exit_code)

      sender ! actors.JobDone(jobID)
  }
}
