package actors

import java.io.{BufferedWriter, File, FileWriter}
import javax.inject.Singleton

import akka.actor.{Actor, ActorRef}
import com.typesafe.config.ConfigFactory
import models.jobs.JobState

import scala.concurrent.Future
import better.files._
import Cmds._
import java.nio.file.attribute.PosixFilePermission

import play.api.Logger

import scala.sys.process.{Process, ProcessLogger}
import scala.util.matching.Regex
import scala.concurrent.ExecutionContext.Implicits.global



/**
  * TODO Inject Database
  *
  * Created by lzimmermann on 10.05.16.
  */
@Singleton
class JobManager extends Actor {

import JobManager._

  val SEP = java.io.File.separator
  val random = scala.util.Random

  // TODO All paths to Config
  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"
  val runscriptPath = s"runscripts$SEP"
  val bioprogsPath = s"${ConfigFactory.load().getString("bioprogs_path")}$SEP"
  val databasesPath = s"${ConfigFactory.load().getString("databases_path")}$SEP"


  // Keeps track of states of Job // TODO Temporary, will be replaced by database
  val jobStates = new collection.mutable.HashMap[Int, JobState.JobState]

  // Keeps track of the job associated tool // TODO Temporary, will be replaced by database
  val jobTools = new collection.mutable.HashMap[Int, String]


  // Keeps track of owner
  val jobOwner = new collection.mutable.HashMap[Int, String]

  // Maps User ID to Actor Ref of corresponding WebSocket
  val connectedUsers = new collection.mutable.HashMap[String, ActorRef]


  //  Generates new jobID // TODO Save this state
  val jobIDSource: Iterator[Int] = Stream.continually(  random.nextInt(8999999) + 1000000 ).distinct.iterator

  // Subdirectories of a Job Directory
  val subdirs = Array("/results", "/logs", "/params", "/specific", "/inter")

  // Ignore the following keys when writing parameters
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")

  // For translating the runscript template into an actual executable runscript
  val argumentPattern = new Regex("""(\$|%|@|\?|\+|\!)\{([^\{\}]+)\}""", "selector", "selValue")


  /**
    * Updates JobState.
    *
    * //TODO Subject to change upon database integration
    *
    * @param jobID
    * @param newState
    * @return
    */
  def changeState(jobID : Int, newState : JobState.JobState) =  {

    Logger.info("Jobstate to be changed")

    jobStates.put(jobID, newState)
    val owner = jobOwner(jobID)
    Logger.info("The owner of the Job is: " + owner)

    // Inform user if connected
    if(connectedUsers contains owner) {

      Logger.info("Will send message to job owner")
      connectedUsers(owner) ! JobStateChanged(jobID, newState)
    }
  }






  // Methods
  def executeJob(jobID : Int, toolname : String, params : Map[String, String]): Unit = {

    // Where the job directory is located
    val rootPath  = s"$jobPath$SEP$jobID$SEP"

    // Where the Runscript of the corresponding tool is located
    val runscript = s"$rootPath$toolname.sh".toFile


    // Parse all lines of runscript template and translate to runscript instance
    for (line <- s"$runscriptPath$toolname.sh".toFile.lines) {

      runscript.appendLine(
        argumentPattern.replaceAllIn(line.split('#')(0), { m =>

          m.group("selector") match {

            case "!" => if (m.group("selValue") == "BIO") bioprogsPath else databasesPath
            case "+" => "inter/" + m.group("selValue")
            case "%" => "params/" + SEP + m.group("selValue")
            case "$" => params.get(m.group("selValue")).get.toString
            case "@" => "results/" + m.group("selValue")
            case "?" =>

              val splt = m.group("selValue").split("(\\||:)")
              if (params.get(splt(0)).get.asInstanceOf[Boolean]) splt(1)
              else {

                if (splt.length == 2) "" else splt(2)
              }
          }
        }))
    }
    chmod_+(PosixFilePermission.OWNER_EXECUTE, runscript)

    // Log files output buffer
    val out = new BufferedWriter(new FileWriter(new File(rootPath + "logs/stdout.out")))
    val err = new BufferedWriter(new FileWriter(new File(rootPath + "logs/stderr.err")))


    val process = Process("./" + toolname + ".sh", new File(rootPath)).run(ProcessLogger(
      (o: String) => out.write(o),
      (e: String) => err.write(e)))

    val exitValue = process.exitValue()

    if(exitValue == 0) {

        changeState(jobID, JobState.Done)
    } else {

        changeState(jobID, JobState.Error)
    }


    out.close()
    err.close()
  }



  def receive : Receive = {


    case UserConnect(userID) =>
      Logger.info("User Connected: " +  userID)
      connectedUsers.put(userID, sender())

    case UserDisconnect(userID) =>
      connectedUsers.remove(userID)



    // User Requests State of Job
    case JobInfo(userID, jobID) =>

      if(!jobStates.contains(jobID)) {

        sender() ! JobIDUnknown

      } else if(!jobOwner(jobID).equals(userID)) {

        sender() ! PermissionDenied

      } else {

        sender() ! (jobStates(jobID), jobTools(jobID))
      }

    //  User asks to delete Job
    case Delete(userID, jobID) =>

      Future {
        // TODO Delete job from database
        s"jobPath$SEP$jobID".toFile.delete(swallowIOExceptions = false)
      }.onComplete {

        case scala.util.Success(_) => sender() ! AckDeleted(jobID)
        case scala.util.Failure(_) => sender() ! FailDeleted(jobID)
      }

     // User asks to prepare new Job
    case Prepare(userID, jobID, toolname, params, start) =>


      Future {

        // Check whether jobID already exists, otherwise make new job

        // This is a new Job Submission // TODO Only support new Jobs currently
        if(jobID.isEmpty) {

          val newJobID = jobIDSource.next()
          val rootPath  = s"$jobPath$SEP$newJobID$SEP"
          jobTools.put(newJobID, toolname)
          jobOwner.put(newJobID, userID)

          changeState(newJobID, JobState.Submitted)

          // Make the directory structure
          subdirs.foreach { s => (rootPath + s).toFile.createDirectories() }

          for((paramName, value) <- params ) {

            if(! ignore.contains(paramName)) {

              s"$rootPath${SEP}params$SEP$paramName".toFile.write(value)
            }
          }
          changeState(newJobID, JobState.Prepared)

          // Also Start Job if requested
          if(start) {
              executeJob(newJobID, toolname, params)
          }
        }
      }
  }
}




object JobManager {

  // User connect and disconnect, mediated by WebSocket
  case class UserConnect(userID : String)

  case class UserDisconnect(userID : String)

  // Failure replies
  case object JobIDUnknown
  case object PermissionDenied
  case class FailDeleted(jobID : Int)

  // Success replies
  case class AckDeleted(jobID : Int)



  // Publish changes JobState
  case class JobStateChanged(jobID : Int, state : JobState.JobState)


  // Ask for jobInfo (toolname and state)
  case class JobInfo(userID : String, jobID  : Int)

  // Delete Job Entirely
  case class Delete(userID : String, jobID : Int)


  // Prepare Job with new parameters or create new job with specified parameters for the given tool
  case class Prepare(userID : String,
                     jobID: Option[Int],
                     toolname : String,
                     params : Map[String, String],
                     start : Boolean)

}
