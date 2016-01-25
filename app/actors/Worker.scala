package actors


import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import helpers.FileAccess
import models._
import java.io.File
import play.api.{Play, Logger}
import scala.sys.process._
import play.api.Play.current



/**
  *
  * Created by lukas on 1/16/16.
  */
class Worker extends Actor with ActorLogging {

  val TOOLNAME = "TOOLNAME"
  val UID = "UID"

  val sep = File.separator
  var path = s"${Play.application.path}${current.configuration.getString("job_path").get}${File.separator}"


  def receive = LoggingReceive {

    case Prepare(spec, jobID, toolname, uid) =>

      path += jobID

      Logger.info("Worker starts to prepare working directory for jobID " + jobID + "\n")

      // create the Working Directory
      FileAccess.mkdir(path)

      Logger.info("Path was " + path)

      for ((key , value) <- spec) {

        FileAccess.mkfile(path + "/" + key, value.toString)
      }
      FileAccess.mkfile(path + "/" + TOOLNAME, toolname)
      FileAccess.mkfile(path + "/" + UID, uid)

      sender ! PrepWDDone(jobID)


    case Start(jobID) =>

      Logger.info("Worker was told to start job" + jobID)

      path += jobID

      // read the toolname file to get the tool configuration
      val toolname = FileAccess.readFile(path + "/" + TOOLNAME)

      // Assemble Bash script

      // parse the exec String and replace the occurrences =of parameters accordingly
      // TODO We need to use reflection here, the map is no reasonable solution


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


      val uid = FileAccess.readFile(path + "/" + UID)

      Logger.info("Job Done for user " + uid)


      // Collect result file paths


      JobManager() ! actors.JobDone(jobID, exit_code, uid, toolname)
  }
}