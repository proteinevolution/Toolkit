package actors

import java.io.{BufferedWriter, FileWriter}
import javax.inject.Singleton

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import models.jobs.JobState

import scala.concurrent.Future
import better.files._
import Cmds._
import java.nio.file.attribute.PosixFilePermission

import models.Constants
import models.tel.TEL
import play.api.Logger

import scala.sys.process._
import scala.concurrent.ExecutionContext.Implicits.global



/**
  * TODO Inject Database
  *
  * Created by lzimmermann on 10.05.16.
  */
@Singleton
class JobManager extends Actor with ActorLogging {

import JobManager._

  val SEP = Constants.SEP
  val random = scala.util.Random

  // TODO All paths to Config
  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"
  val runscriptPath = s"TEL${SEP}runscripts$SEP"
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

  // Ignore the following keys when writing parameters
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")


  // Keeps track of all running processes. // TODO Should be restored after toolkit reboots
  val runningProcesses = new collection.mutable.HashMap[Int, Process]



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

    val rootPath  = s"$jobPath$SEP$jobID$SEP"
    val runscript = s"$rootPath$toolname.sh"

    chmod_+(PosixFilePermission.OWNER_EXECUTE, runscript.toFile)

    Logger.info("Runscript " + runscript)

    // Log files output buffer
    val out = new BufferedWriter(new FileWriter(new java.io.File(rootPath + "logs/stdout.out")))
    val err = new BufferedWriter(new FileWriter(new java.io.File(rootPath + "logs/stderr.err")))

    // Job will now be executed, change the job state to running
    changeState(jobID, JobState.Running)

    // Create new Process instance of the runscript to run the tool
    val process = Process(runscript , new java.io.File(rootPath)).run(ProcessLogger(
      (fout) => out.write(fout),
      (ferr) => err.write(ferr)
    ))
    runningProcesses.put(jobID, process)

    if(process.exitValue() == 0) {

        changeState(jobID, JobState.Done)
    } else {

        changeState(jobID, JobState.Error)
    }
    runningProcesses.remove(jobID)

    out.close()
    err.close()
  }



  def receive : Receive = {


    case UserConnect(userID) =>
      Logger.info("User Connected: " +  userID)
      connectedUsers.put(userID, sender())

    case UserDisconnect(userID) =>
      connectedUsers.remove(userID)

     // Reads parameters provided to the job from the job directory
    case Read(userID, jobID) =>

        // If jobID is unknown
        if (!jobStates.contains(jobID)) {

          sender() ! JobIDUnknown

          // If jobID does not belong to the user
        } else if (!jobOwner(jobID).equals(userID)) {

          sender() ! PermissionDenied

        } else {

          sender() ! s"$jobPath$SEP$jobID${SEP}params".toFile.list.map { f =>

            f.name -> f.contentAsString

          }.toMap
        }



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

      //  Terminate running Process instance of the Job
      if(runningProcesses.contains(jobID)) {

        runningProcesses(jobID).destroy()
      }

      Future {
        // TODO Delete job from database

        // Delete Job Path
        s"jobPath$SEP$jobID".toFile.delete(swallowIOExceptions = false)
      }.onComplete {

        case scala.util.Success(_) => sender() ! AckDeleted(jobID)
        case scala.util.Failure(_) => sender() ! FailDeleted(jobID)
      }

     // User asks to prepare new Job, might be directly executed (if start is true)
    case Prepare(userID, jobID, toolname, params, start) =>

      Future {
        // Check whether jobID already exists, otherwise make new job

        // This is a new Job Submission // TODO Only support new Jobs currently
        if(jobID.isEmpty) {

          val newJobID = jobIDSource.next()
          val rootPath  = s"$jobPath$SEP$newJobID$SEP"

          // TODO Replace by database
          jobTools.put(newJobID, toolname)
          jobOwner.put(newJobID, userID)

          changeState(newJobID, JobState.Submitted)

          // Use an interface of TEL to initialize a Job Directory
          TEL.init(toolname, params, rootPath)

          // Job Directory will then be prepared
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




  // Reads the parameters from a prepared job and provides them to the user
  case class Read(userID : String, jobID : Int)

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
