package master

import java.io
import java.io.{BufferedWriter, FileWriter}

import akka.actor.{Actor, ActorLogging, Props}
import better.files._
import com.typesafe.config.ConfigFactory
import models.distributed.FrontendMasterProtocol.{Delete, Prepare, PrepareAndStart}
import models.distributed.Work
import models.graph.Ready
import models.jobs.{Done, Error, Running}

import scala.sys.process._



object WorkExecutor {


  def props() = Props(new WorkExecutor())

}

/**
  * Created by lzimmermann on 02.04.16.
  */
class WorkExecutor extends Actor with ActorLogging {


  val SEP = java.io.File.separator

  // Directory Names in Job Directory
  val PARAM_DIR = "params"
  val LOG_DIR = "logs"

  val subdirs = Array("/results", "/logs", "/params", "/specific", "/inter")

  // Get paths
  val runscriptPath = s"master${SEP}runscripts$SEP"
  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"
  val bioprogsPath = s"${ConfigFactory.load().getString("bioprogs_path")}$SEP"
  val databasesPath = s"${ConfigFactory.load().getString("databases_path")}$SEP"

  val argumentPattern = "(\\$\\{[a-z_]+\\}|#\\{[a-z_]+\\}|@\\{[a-z_]+\\}|\\?\\{.+\\}|\\+\\{[a-z_]+\\}|\\!\\{(BIO|DATA)\\})".r


  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")


  def receive : Receive =  {


   case work@Work(workId, userRequest, userJob) =>

      userRequest match {

        case Prepare(sessionID, jobID, toolname, params, newJob) =>


          // Assemble the path were the Job directory is located
          val rootPath  = s"$jobPath$SEP${userJob.mainID.toString}$SEP"

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

        case PrepareAndStart(sessionID, jobID, toolname, params, newJob) =>



          // PREPARE STEP
          // Assemble the path were the Job directory is located
          val rootPath  = s"$jobPath$SEP${userJob.mainID.toString}$SEP"

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

          //START STEP
          val mainID = userJob.mainID
          val paramPath = s"$jobPath${mainID.toString}$SEP$PARAM_DIR"

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

          userJob.changeState(Running)

          // Log files output buffer
          val out = new BufferedWriter(new FileWriter(new io.File(rootPath + "logs/stdout.out")))
          val err = new BufferedWriter(new FileWriter(new io.File(rootPath + "logs/stderr.err")) )

          log.info("Start tool " + userJob.tool.toolname)
          val process = Process("./" + userJob.tool.toolname + ".sh", new io.File(rootPath)).run(ProcessLogger(
            (o: String) => out.write(o),
            (e: String) => err.write(e)))

          userJob.process = Some(process)
          val exitValue = process.exitValue()

          if(exitValue != 0) {

              userJob.changeState(Error)
          } else {

              userJob.changeState(Done)
            }
          out.close()
          err.close()


        case Delete(sessionID, jobID) =>

          // Delete the Job path recursively
          s"$jobPath${userJob.mainID}$SEP".toFile.delete(swallowIOExceptions = false)


      }

      // Tell the worker that the work was complete
     sender() ! Worker.WorkComplete
  }
}
