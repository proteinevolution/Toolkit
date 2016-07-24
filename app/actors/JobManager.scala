package actors

import java.io.{BufferedWriter, FileWriter}
import java.util.{Date}
import javax.inject.{Inject, Singleton}


import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import models.jobs.JobState
import play.api.i18n.MessagesApi

import reactivemongo.api.collections.bson.BSONCollection


import reactivemongo.bson.BSONDocument

import scala.concurrent.Future
import better.files._

import models.{Constants, ExitCodes}
import models.tel.TEL
import play.api.Logger

import scala.sys.process._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Try, Success, Failure}
import play.api.libs.json.Json
import play.api.mvc.{ Action, Controller, Request }

import play.modules.reactivemongo.{
MongoController, ReactiveMongoApi, ReactiveMongoComponents
}


/**
  * TODO Inject Database
  *
  * Created by lzimmermann on 10.05.16.
  */
@Singleton
class JobManager @Inject() (
                             val messagesApi: MessagesApi,
                             val reactiveMongoApi: ReactiveMongoApi,
                             implicit val materializer: akka.stream.Materializer) extends Actor with ActorLogging with MongoController with ReactiveMongoComponents {

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

    jobStates.put(jobID, newState)
    val owner = jobOwner(jobID)

    // Inform user if connected
    if(connectedUsers contains owner) {

      connectedUsers(owner) ! JobStateChanged(jobID, newState)
    }
  }

  /**
    * @param jobID
    * @param scriptPath
    */
  def executeJob(jobID : Int, scriptPath : String): Unit = {

    val rootPath  = s"$jobPath$SEP$jobID$SEP"

    // Log files output buffer
    val out = new BufferedWriter(new FileWriter(new java.io.File(rootPath + "logs/stdout.out")))
    val err = new BufferedWriter(new FileWriter(new java.io.File(rootPath + "logs/stderr.err")))

    // Job will now be executed, change the job state to running
    changeState(jobID, JobState.Running)

    // Create new Process instance of the runscript to run the tool
    val process = Process(scriptPath , new java.io.File(rootPath)).run(ProcessLogger(
      (fout) => out.write(fout),
      (ferr) => err.write(ferr)
    ))
    runningProcesses.put(jobID, process)

    // Treat Exit code of job process
    process.exitValue() match {

      case ExitCodes.SUCCESS => changeState(jobID, JobState.Done)
      case ExitCodes.TERMINATED => // Ignore
      case x: Int => changeState(jobID, JobState.Error)
    }
    runningProcesses.remove(jobID)

    out.close()
    err.close()
  }



  import reactivemongo.play.json._



  val jobCollection: BSONCollection = db.collection("jobs")


  def delete(id: Int) = {

    jobCollection.remove(Json.obj("main_id" -> id))

  }




  def receive : Receive = {


    case UserConnect(userID) => connectedUsers.put(userID, sender())

    case UserDisconnect(userID) => connectedUsers.remove(userID)

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

        delete(jobID)

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

          val document = BSONDocument(
            "main_id" -> newJobID, //this is wrong, I know, it should be the job_id
            "tool" -> toolname,
            "user_id" -> userID,
            "created_on" -> new Date(),
            "update_on" -> new Date(),
            "viewed_on" -> 0)

          val future = jobCollection.insert(document)


          future.onComplete {
            case Failure(e) => throw e
            case Success(lastError) => {
              println("successfully inserted document with lastError = " + lastError)
            }
          }

          changeState(newJobID, JobState.Submitted)

          // Use an interface of TEL to initialize a Job Directory
          val script = TEL.init(toolname, params, rootPath)

          // Job Directory will then be prepared
          changeState(newJobID, JobState.Prepared)

          // Also Start Job if requested
          if(start) {
              executeJob(newJobID, script)
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
