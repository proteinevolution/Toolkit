package actors

import java.io.{BufferedWriter, FileWriter}
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.typesafe.config.ConfigFactory
import models.database.{Job, User}
import models.database.Job.JobReader
import models.database.JobState
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.concurrent.Future
import better.files._
import models.{Constants, ExitCodes}
import models.tel.TEL
import play.api.Logger
import play.api.libs.json.Json

import scala.sys.process._
import scala.concurrent.ExecutionContext.Implicits.global
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}

/**
  * Created by lzimmermann on 10.05.16.
  */
@Singleton
final class JobManager @Inject() (val messagesApi: MessagesApi,
                                  val reactiveMongoApi: ReactiveMongoApi,
                                  implicit val materializer: akka.stream.Materializer)
  extends Actor with ActorLogging with ReactiveMongoComponents with Constants with ExitCodes {

  import JobManager._


  def jobBSONCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("jobs"))
  def userBSONCollection = reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))

  val random = scala.util.Random


  // TODO All paths to Config
  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEPARATOR"
  val runscriptPath = s"TEL${SEPARATOR}runscripts$SEPARATOR"

  // Maps Session ID to Actor Ref of corresponding WebSocket
  val connectedUsers =  new scala.collection.mutable.HashMap[BSONObjectID, ActorRef]

  //  Generates new jobID // TODO Save this state
  val jobIDSource: Iterator[Int] = Stream.continually(  random.nextInt(8999999) + 1000000 ).distinct.iterator

  // Ignore the following keys when writing parameters
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")


  // Keeps track of all running processes. // TODO Should be restored after toolkit reboots
  val runningProcesses = new collection.mutable.HashMap[String, Process]

  implicit val reader = JobReader


  /**
    * Updates Job in database or creates a new Job if job with mainID does not exist
    *
    * @param newJob
    */
  def updateJob(newJob : Job): Unit = {

     jobBSONCollection.flatMap(_.find(BSONDocument(Job.IDDB -> newJob.mainID)).one[Job]).map {

       case Some(oldJob) =>

         if(oldJob.status != newJob.status && connectedUsers.contains(newJob.sessionID)) {

           connectedUsers(newJob.sessionID) ! JobStateChanged(newJob, newJob.status)
         }
       jobBSONCollection.flatMap(_.update(BSONDocument(Job.IDDB -> newJob.mainID),
           BSONDocument("$set" -> newJob)))

       case None => jobBSONCollection.flatMap(_.insert(newJob))
     }
  }



  /**
    * @param job
    * @param scriptPath
    */
  def executeJob(job : Job, scriptPath : String): Unit = {

    val rootPath  = s"$jobPath$SEPARATOR${job.mainID.stringify}$SEPARATOR"

    // Log files output buffer
    val out = new BufferedWriter(new FileWriter(new java.io.File(rootPath + "logs/stdout.out")))
    val err = new BufferedWriter(new FileWriter(new java.io.File(rootPath + "logs/stderr.err")))

    // Job will now be executed, change the job state to running

    this.updateJob(job.copy(status = JobState.Running))

    // Create new Process instance of the runscript to run the tool
    val process = Process(scriptPath , new java.io.File(rootPath)).run(ProcessLogger(
      (fout) => out.write(fout),
      (ferr) => err.write(ferr)
    ))
    runningProcesses.put(job.mainID.stringify, process)

    // Treat Exit code of job process
    process.exitValue() match {

      case SUCCESS => updateJob(job.copy(status = JobState.Done))
      case TERMINATED => // Ignore
      case x: Int => updateJob(job.copy(status = JobState.Error))
    }
    runningProcesses.remove(job.mainID.stringify)

    out.close()
    err.close()
  }

  def receive : Receive = {

    // User Connected, add them to the connected users list
    case UserConnect(sessionID) =>
      //Logger.info("User Connected: " + sessionID.stringify)
      val _ = connectedUsers.getOrElseUpdate(sessionID, sender())
      //this.connectedUsers = connectedUsers.updated(sessionID, sender())

    // User Disconnected, Remove them from the connected users list.
    case UserDisconnect(sessionID) =>
      //Logger.info("User Disconnected: " + sessionID.stringify)
      val _ = connectedUsers.remove(sessionID)

    // Get a request to send the job list
    case GetJobList(sessionID, user) =>

      // Find all jobs related to the session ID
      val futureJobs = jobBSONCollection.map(_.find(BSONDocument(Job.SESSIONID -> sessionID)).cursor[Job]())
      // Collect the list and then create the reply
      val replyTo = sender()
      futureJobs.flatMap(_.collect[List]()).foreach { jobList =>
        //Logger.info("Found " + jobList.length.toString + " Job[s]. Sending.")
        replyTo ! SendJobList(jobList)
      }


     // Reads parameters provided to the job from the job directory
    case Read(sessionID : BSONObjectID, jobID : String) =>
      jobBSONCollection.flatMap(_.find(BSONDocument(Job.JOBID -> jobID)).one[Job]).foreach {
        case Some(job) => // Job Owner must be linked with the Session ID
          if (job.sessionID == sessionID) // Retrieve the Job Files
            sender () ! s"$jobPath$SEPARATOR${job.mainID.stringify}${SEPARATOR}params".toFile.list.map {f =>
              f.name -> f.contentAsString
            }.toMap
          else // If jobID does not belong to the user
            sender () ! PermissionDenied
        case None => // If jobID is unknown
          sender () ! JobIDUnknown
      }


    // User Requests State of Job
    // TODO Move this logic to the Controller
    case JobInfo(sessionID : BSONObjectID, jobID) =>
      val replyTo = sender()
        jobBSONCollection.flatMap(_.find(BSONDocument(Job.JOBID -> jobID)).one[Job]).foreach {
          case Some(job) => // Job Owner must be linked with the Session ID
            if (job.sessionID == sessionID) // Retrieve the Job Files
              replyTo ! job
            else // If jobID does not belong to the user
              replyTo ! PermissionDenied
          case None => // If jobID is unknown
            replyTo ! JobIDUnknown
        }

    //  User asks to delete Job
    case Delete(sessionID, jobID) =>
      jobBSONCollection.flatMap(_.find(BSONDocument(Job.JOBID -> jobID)).one[Job]).foreach {
        case Some(job) =>
          if (job.sessionID == sessionID) {

            //  Terminate running Process instance of the Job
            if (runningProcesses.contains(job.mainID.stringify)) {
              runningProcesses(job.mainID.stringify).destroy()
            }
            jobBSONCollection.flatMap(_.remove(BSONDocument(Job.IDDB -> job.mainID)))

            Future {
              // Delete Job Path
              s"jobPath$SEPARATOR${job.mainID.stringify}".toFile.delete(swallowIOExceptions = false)
            }.onComplete {
              case scala.util.Success(_) => sender() ! AckDeleted(jobID)
              case scala.util.Failure(_) => sender() ! FailDeleted(jobID)
            }
          } else {
            sender() ! PermissionDenied
          }
        case None      =>
          // Job ID is unknown.
          sender() ! JobIDUnknown
      }

    case UpdateJob(job)  =>


      Logger.info("Job Manager was asked to update Job")




    // User asks to prepare new Job, might be directly executed (if start is true)
    case Prepare(sessionID, jobID, toolName, params, start) =>
      Future {
      // Check whether jobID already exists, otherwise make new job
      // This is a new Job Submission // TODO Only supports new Jobs currently
      if(jobID.isEmpty) {

        val jobCreationTime = DateTime.now()
        val newJob = Job(mainID      = BSONObjectID.generate(),
                         jobType     = "",
                         parentID    = None,
                         jobID       = jobIDSource.next().toString, //TODO Refactor to name
                         sessionID   = sessionID,
                         userID      = None,
                         status      = JobState.Submitted,
                         tool        = toolName,
                         statID      = "",
                         dateCreated = Some(jobCreationTime),
                         dateUpdated = Some(jobCreationTime),
                         dateViewed  = Some(jobCreationTime))

        this.updateJob(newJob)

        // Interfaces with TEL to make a new job directory, returns the  path to the script which then
        // needs to be executed
        val rootPath = s"$jobPath$SEPARATOR${newJob.mainID.stringify}$SEPARATOR"

        val script = TEL.init(toolName, params, rootPath)
        this.updateJob(newJob.copy(status = JobState.Prepared))

        // Write a JSON File with the job information to the JobDirectory
        s"$rootPath$jobJSONFileName".toFile.write(Json.toJson(newJob).toString())


        // Also Start Job if requested
        if(start) {
          executeJob(newJob, script)
        }
      }
  }
  }
}

object JobManager {
  // User connect preparation, mediated by WebSocket
  case class UserConnect(sessionID : BSONObjectID)

  // User disconnect cleanup, mediated by WebSocket
  case class UserDisconnect(sessionID : BSONObjectID)

  // Tell job widget to update
  case object UpdateAllJobs

  // When the JobManager was asked to update a Job
  case class UpdateJob(job : Job)

  // Get a request to send the job list
  case class GetJobList(sessionID : BSONObjectID, user : Option[User])

  // Send the job list to the user
  case class SendJobList(jobList : List[Job])

  // Failure replies
  case object JobIDUnknown
  case object PermissionDenied
  case class FailDeleted(jobID : String)

  // Success replies
  case class AckDeleted(jobID : String)

  // Reads the parameters from a prepared job and provides them to the user
  case class Read(userID : BSONObjectID, jobID : String)

  // Publish changes JobState
  case class JobStateChanged(job : Job, state : JobState.JobState)

  // Ask for jobInfo (tool name and state)
  case class JobInfo(userID : BSONObjectID, jobID  : String)

  // Delete Job Entirely
  case class Delete(userID : BSONObjectID, jobID : String)

  // Prepare Job with new parameters or create new job with specified parameters for the given tool
  case class Prepare(userID   : BSONObjectID,
                     jobID    : Option[String],
                     toolname : String,
                     params   : Map[String, String],
                     start    : Boolean)
}
