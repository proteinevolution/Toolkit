package actors


import java.io
import java.io.PrintWriter
import java.nio.file.{StandardOpenOption, Paths, Files}
import javax.inject.Inject

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive

import com.typesafe.config.ConfigFactory

import models.graph.{Link, PortWithFormat, Ports, Ready}
import models.jobs._


import play.api.Logger
import utils.Exceptions.{RunscriptExecutionFailedException, NotImplementedException}
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.io.Source
import scala.sys.process._
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

// TODO Get rid of this library
import scala.reflect.io.{Directory, File}


/* TODO Worker specific TODOs
 * Get rid of Reflect Library since its state is considered to be experimental
 *
*/

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
      subdirs.foreach { s => Files.createDirectories(Paths.get(rootPath + s))  }

      ///
      ///          Write the parameters into the directory, this will also change the state
      ///          of the job as a side effect. We will write all parameters except for the jobid
      ///          which currently also occurs in the Map
      for((paramName, value) <- params ) {


        if(! ignore.contains(paramName)) {

          Logger.info("Worker will write " + paramName)

          Files.write(Paths.get(s"${rootPath}params$SEP$paramName"), value.getBytes("utf-8"),
                                        StandardOpenOption.CREATE,
                                        StandardOpenOption.TRUNCATE_EXISTING)
          // The argument has been written to the file, so we can change the state of the infile to Ready
          // The job should not care whether the File was already set to ready
          Logger.info("Worker will change file State")
          userJob.changeInFileState(paramName, Ready)

        }
      }

    case WRead(userJob) =>

      val main_id_o = jobDB.getMainID(userJob.user_id, userJob.job_id)
      main_id_o match {
        case Some(main_id) =>
          val paramPath = jobPath + main_id + SEP + "params/"

          val files = new java.io.File(paramPath).listFiles

          val res : Map[String, String] = files.map { file =>

            file.getName -> scala.io.Source.fromFile(file.getAbsolutePath).mkString
          }.toMap

          sender() ! res
        case None =>
          userJob.changeState(Error)
      }


    case WDelete(userJob) =>

      val dbJobOption = jobDB.delete(userJob.user_id, userJob.job_id)

      dbJobOption match {
        case Some(dbJob) =>
          val rootPath  = jobPath + dbJob.main_id.get + SEP // .get is ok
          Directory(rootPath).deleteRecursively()
          jobRefDB.delete(dbJob.main_id.get) // .get is ok
        case None =>
          userJob.changeState(Error)
      }


    case WConvert(parentUserJob, childUserJob, links) =>

      // Assemble all necessary file paths
      val parent_main_id_o = jobDB.getMainID(parentUserJob.user_id, parentUserJob.job_id)
      val child_main_id_o = jobDB.getMainID(childUserJob.user_id, childUserJob.job_id)

      parent_main_id_o match {
        case Some(parent_main_id) =>
          child_main_id_o match {
            case Some(child_main_id) =>
              val parentRootPath = jobPath + parent_main_id + SEP
              val childRootPath = jobPath + child_main_id + SEP

      // Create Child Root Path if does not already exist
      if(!java.nio.file.Files.exists(Paths.get(childRootPath))) {


                Directory(childRootPath).createDirectory(false, false)
                for (subdir <- subdirs) {

                  Directory(childRootPath + subdir).createDirectory(false, false)
                }
              }

              for(link <- links) {

                val outport = parentUserJob.tool.outports(link.out)
                val inport = childUserJob.tool.inports(link.in)

                val params : Option[ArrayBuffer[String]] = Ports.convert(outport, inport)

                // Assemble paths to respective files
                val outfile = parentRootPath + "results/" + outport.filename
                val infile =  childRootPath + "params/" + inport.filename

                // Decide whether conversion is needed
                params match  {

                  // This is the same format, just copy over the file
                  case None =>
                    java.nio.file.Files.copy(Paths.get(outfile), Paths.get(infile))
                    childUserJob.changeInFileState(inport.filename, Ready)

                    // If this port has a format, we also need to write the format file
                    if(inport.isInstanceOf[PortWithFormat]) {

                      val portWithFormat = inport.asInstanceOf[PortWithFormat]
                      File(s"$childRootPath${SEP}params$SEP${portWithFormat.formatFilename}").writeAll(portWithFormat.format.paramName)
                      childUserJob.changeInFileState(portWithFormat.formatFilename, Ready)
                    }

                  case Some(buffer) => throw NotImplementedException("Format conversion is currently not supported")

                }
              }
            case None =>
              childUserJob.changeState(Error)
          }
        case None =>
          parentUserJob.changeState(Error)
      }


    case WStart(userJob) =>

      Logger.info("[Worker](WStart) for job " + userJob.job_id)
      val main_id_o = jobDB.getMainID(userJob.user_id, userJob.job_id)
      main_id_o match {
        case Some(main_id) =>
          val rootPath  = jobPath + main_id.toString + SEP
          val paramPath = jobPath + main_id.toString + SEP + "params/"


          val files = new java.io.File(paramPath).listFiles

          val params : Map[String, String] = files.map { file =>

            file.getName -> scala.io.Source.fromFile(file.getAbsolutePath).mkString
          }.toMap

          ///
          ///  Step 2: Get the runscript of the appropriate tool and replace template placeholders with
          ///  input parameters
          val sourceRunscript = Source.fromFile(runscriptPath + userJob.tool.toolname + ".sh")
          val targetRunscript = new PrintWriter(rootPath + userJob.tool.toolname + ".sh")

          for(line <- sourceRunscript.getLines) {

            targetRunscript.println(argumentPattern.replaceAllIn(line, { rm =>

              val s = rm.toString()
              val value = s.substring(2, s.length - 1)

              s(0) match {

                case '!' => if(value == "BIO") bioprogsPath else databasesPath
                case '+' => "inter/" + value
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
          Logger.info("Set file permission of: " + rootPath + userJob.tool.toolname + ".sh")
          ("chmod u+x " + rootPath + userJob.tool.toolname + ".sh").!    // TODO Is there a neater way to change the permission?
          sourceRunscript.close()
          targetRunscript.close()



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



        case None =>
          userJob.changeState(Error)
      }
  }
}
