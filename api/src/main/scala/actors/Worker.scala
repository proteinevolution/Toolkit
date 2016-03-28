package actors


import java.io
import better.files._
import javax.inject.Inject
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import com.typesafe.config.ConfigFactory
import models.graph.{Link, PortWithFormat, Ports, Ready}
import models.jobs._
import utils.Exceptions.{RunscriptExecutionFailedException, NotImplementedException}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.sys.process._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global


object Worker {

  // Prepare the Working Directory with new Values for the parameters.
  case class WPrepare(job : UserJob, params : Map[String, String])

  // Executes the specified job.
  case class WStart(job: UserJob)

  // Deletes the Job, such that the working directory will be brutally removed
  case class WDelete(job : UserJob)

  // Worker will read the parameters of the job from the working directory and tell them back in a Map
  case class WRead(job : UserJob)

  // Worker was asked to convert all provided links between the provided Jobs
  case class WConvert(parentUserJob : UserJob, childUserJob : UserJob, links : Seq[Link])
}

class Worker @Inject() (jobDB    : models.database.Jobs,
                        jobRefDB : models.database.JobReference)
  extends Actor with ActorLogging {

  import actors.Worker._

  val SEP = java.io.File.separator

  // Directory Names in Job Directory
  val PARAM_DIR = "params"


  val subdirs = Array("/results", "/logs", "/params", "/specific", "/inter")

  // Get paths
  val runscriptPath = s"cluster${SEP}runscripts$SEP"
  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"
  val bioprogsPath = s"${ConfigFactory.load().getString("bioprogs_path")}$SEP"
  val databasesPath = s"${ConfigFactory.load().getString("databases_path")}$SEP"

  val argumentPattern = "(\\$\\{[a-z_]+\\}|#\\{[a-z_]+\\}|@\\{[a-z_]+\\}|\\?\\{.+\\}|\\+\\{[a-z_]+\\}|\\!\\{(BIO|DATA)\\})".r


  val ignore : Seq[String] = Array("jobid", "newSubmission", "start", "edit")


  def receive = LoggingReceive {

    case WPrepare(userJob, params) =>

      val main_id = jobDB.getMainID(userJob.user_id, userJob.job_id).get

      // Assemble the path were the Job directory is located
      val rootPath  = s"$jobPath$SEP${main_id.toString}$SEP"

      // If the Job directory does not exist yet, make a new one
      subdirs.foreach { s => (rootPath + s).toFile.createDirectories() }
      ///
      ///          Write the parameters into the directory, this will also change the state
      ///          of the job as a side effect. We will write all parameters except for the jobid
      ///          which currently also occurs in the Map
      for((paramName, value) <- params ) {

        if(! ignore.contains(paramName)) {

          s"$rootPath$SEP$PARAM_DIR$SEP$paramName".toFile.write(value)
          // The argument has been written to the file, so we can change the state of the infile to Ready
          // The job should not care whether the File was already set to ready
          userJob.changeInFileState(paramName, Ready)
        }
      }

    case WRead(userJob) =>

      val main_id_o = jobDB.getMainID(userJob.user_id, userJob.job_id)
      main_id_o match {

        case Some(main_id) =>

          sender() ! s"$jobPath$main_id$SEP$PARAM_DIR".toFile.list.map { file =>

            file.name -> file.contentAsString
          }.toMap

        case None =>
          userJob.changeState(Error)
      }


    case WDelete(userJob) =>

      val dbJob = jobDB.delete(userJob.user_id, userJob.job_id).get
      s"$jobPath${dbJob.main_id.get}$SEP".toFile.delete(swallowIOExceptions = false) // get is safe, Delete JobDir
      jobRefDB.delete(dbJob.main_id.get) // .get is ok




    case WConvert(parentUserJob, childUserJob, links) =>

      // Assemble all necessary file paths
      val parent_main_id = jobDB.getMainID(parentUserJob.user_id, parentUserJob.job_id).get
      val child_main_id = jobDB.getMainID(childUserJob.user_id, childUserJob.job_id).get

      val parentRootPath = s"$jobPath$parent_main_id$SEP"
      val childRootPath = s"$jobPath$child_main_id$SEP"

      // If the Job directory does not exist yet, make a new one
      subdirs.foreach { s => (childRootPath + s).toFile.createDirectories() }

      for(link <- links) {

        val outport = parentUserJob.tool.outports(link.out)
        val inport = childUserJob.tool.inports(link.in)

        val params : Option[ArrayBuffer[String]] = Ports.convert(outport, inport)

        // Assemble paths to respective files
        val outfile = s"${parentRootPath}results/${outport.filename}"
        val infile =  s"$childRootPath$SEP$PARAM_DIR$SEP${inport.filename}"

        // Decide whether conversion is needed
        params match  {

          // This is the same format, just copy over the file
          case None =>

            outfile.toFile.copyTo(infile.toFile)
            childUserJob.changeInFileState(inport.filename, Ready)

            // If this port has a format, we also need to write the format file
            inport match {

              case portWithFormat : PortWithFormat =>

                s"$childRootPath$SEP$PARAM_DIR$SEP${portWithFormat.formatFilename}".toFile.write(portWithFormat.format.paramName)
                childUserJob.changeInFileState(portWithFormat.formatFilename, Ready)
            }

          case Some(buffer) => throw NotImplementedException("Format conversion is currently not supported")
        }
      }


    case WStart(userJob) =>

      val main_id = jobDB.getMainID(userJob.user_id, userJob.job_id).get

      val rootPath  = s"$jobPath${main_id.toString}$SEP"
      val paramPath = s"$jobPath${main_id.toString}$SEP$PARAM_DIR"

      val params : Map[String, String] = paramPath.toFile.list.map { file =>

            file.name -> file.contentAsString
      }.toMap

      ///
      ///  Step 2: Get the runscript of the appropriate tool and replace template placeholders with
      ///  input parameters
      for(line <- s"$runscriptPath${userJob.tool.toolname}.sh".toFile.lines) {

        s"$rootPath${userJob.tool.toolname}.sh".toFile.appendLine(argumentPattern.replaceAllIn(line, { rm =>

              val s = rm.toString()
              val value = s.substring(2, s.length - 1)

              s(0) match {

                case '!' => if(value == "BIO") bioprogsPath else databasesPath
                case '+' => "inter/" + value
                case '#' =>  PARAM_DIR + SEP + value
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
      ("chmod u+x " + rootPath + userJob.tool.toolname + ".sh").!    // TODO Is there a neater way to change the permission?


      // Run the tool async in the execution context of the worker
      Future {

        userJob.changeState(Running)
        val process = Process("./" + userJob.tool.toolname + ".sh", new io.File(rootPath)).run
        userJob.process = Some(process)
        val exitValue = process.exitValue()

        if(exitValue != 0) {

          throw RunscriptExecutionFailedException(exitValue, "Execution of Runscript failed. Exit code was " + exitValue)
        } else {

          exitValue
        }
      } onComplete {

        case Success(_) => userJob.changeState(Done)

              // TODO Add more Error handling here
        case Failure(t) => userJob.changeState(Error)
      }
  }
}
