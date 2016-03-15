package actors


import java.io
import java.io.PrintWriter
import java.nio.file.{Paths, Files}
import javax.inject.Inject
import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import models.graph.{PortWithFormat, Ports, Ready}
import models.jobs._
import play.api.Logger
import utils.Exceptions.NotImplementedException
import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import sys.process._


// TODO Get rid of this library
import scala.reflect.io.{Directory, File}


/* TODO Worker specific TODOs
 * Get rid of Reflect Library since its state is considered to be experimental
 *
*/

object Worker {

  // Preparation of the Job
  case class WPrepare(job : UserJob, params : Map[String, String])
  // Starting the job
  case class WStart(job: UserJob)

  case class WDelete(job : UserJob)

  // Worker was asked to read parameters of the job and to put them into a Map
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


  // Variables the worker need to execute //TODO Inject Configuration
  val runscriptPath = s"bioprogs${SEP}runscripts$SEP"
  val jobPath = s"development$SEP"
  val argumentPattern = "(\\$\\{[a-z_]+\\}|#\\{[a-z_]+\\}|@\\{[a-z_]+\\}|\\?\\{.+\\}|\\+\\{[a-z_]+\\})".r


  def receive = LoggingReceive {

    case WPrepare(userJob, params) =>

      Logger.info("[Worker](WPrepare) for job " + userJob.job_id)
      Logger.info("[Worker] Runscript path was " + runscriptPath)
      Logger.info("[Worker] Job path was " + jobPath)

      val main_id_o = jobDB.getMainID(userJob.user_id, userJob.job_id)
      main_id_o match {
        case Some(main_id) =>
          val rootPath  = jobPath + SEP + main_id.toString + SEP

          ///
          ///  Step 1: Make the working directory of the job with all subdirectories
          ///
          if(!Files.exists(Paths.get(rootPath))) {
            Directory(rootPath).createDirectory(false, false)
            for (subdir <- subdirs) {

              Directory(rootPath + subdir).createDirectory(false, false)
            }
          }


          ///
          ///  Step 3: Write the parameters into the directory, this will also change the state
          ///          of the job as a side effect.
          for( (paramName, value) <- params ) {

            if(paramName != "jobid") {
              File(s"$rootPath${SEP}params$SEP$paramName").writeAll(value.toString)
              userJob.changeInFileState(paramName, Ready)
            }
          }
          Logger.info("All params were written to the job_directory successfully")
        case None =>
          userJob.changeState(Error)
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
          val sourceRunscript = Source.fromFile(runscriptPath + userJob.toolname + ".sh")
          val targetRunscript = new PrintWriter(rootPath + userJob.toolname + ".sh")

          for(line <- sourceRunscript.getLines) {

            targetRunscript.println(argumentPattern.replaceAllIn(line, { rm =>

              val s = rm.toString()
              val value = s.substring(2, s.length - 1)

              s(0) match {

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
          Logger.info("Set file permission of: " + rootPath + userJob.toolname + ".sh")
          ("chmod u+x " + rootPath + userJob.toolname + ".sh").!    // TODO Is there a neater way to change the permission?
          sourceRunscript.close()
          targetRunscript.close()


          // TODO Maybe we can use the Process builder in a more clever way

          // Actually start the Job, the code will stall here
          userJob.changeState(Running)
          val result = Process("./" + userJob.toolname + ".sh", new io.File(rootPath)).!

          // Change state of job depending on the RUnscript execution
          // TODO Add more error handling here
          userJob.changeState(if(result == 0) Done else Error)
        case None =>
          userJob.changeState(Error)
      }
  }
}
