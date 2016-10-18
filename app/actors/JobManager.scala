package actors

import java.io.{BufferedWriter, FileWriter}
import javax.inject.{Inject, Named, Singleton}

import actors.UserManager.{JobAdded, MessageWithUserID}
import akka.actor.{Actor, ActorLogging, ActorRef}
import models.database.{Job, JobHash, JobState, User}
import models.search.JobDAO
import modules.Common
import modules.tools.FNV
import org.joda.time.DateTime
import play.api.i18n.MessagesApi
import reactivemongo.bson.{BSONDocument, BSONObjectID, BSONValue}

import scala.concurrent.Future
import better.files._
import models.database.JobState.JobState
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
                                  @Named("userManager") userManager : ActorRef,
                                  val tel : TEL,
                                  val jobDao : JobDAO,
                                  implicit val materializer: akka.stream.Materializer)
  extends Actor with ActorLogging with ReactiveMongoComponents with Constants with ExitCodes with Common {

  import JobManager._

  val random = scala.util.Random

  //  Generates new jobID // TODO Save this state
  val jobIDSource: Iterator[Int] = Stream.continually(  random.nextInt(8999999) + 1000000 ).distinct.iterator

  // Ignore the following keys when writing parameters
  val ignore: Seq[String] = Array("jobid", "newSubmission", "start", "edit")


  // Keeps track of all running processes. // TODO Should be restored after toolkit reboots
  val runningProcesses = new collection.mutable.HashMap[String, Process]

  /**
    * Updates Job in database or creates a new Job if job with mainID does not exist
    * TODO refactor this to take less database accesses - only insert or find & update
    *
    * @param job
    */
  def updateJob(job : Job) = {
    // Check if there already is a job with the mainID
    findJob(BSONDocument(Job.IDDB -> job.mainID)).foreach {
      // edit the old job
      case Some(oldJob) =>
        if(oldJob.status != job.status) {
          // Inform the users about the change
          userManager ! JobStateChanged(job, job.status)
        }
        // edit the job state in the database
        modifyJob(BSONDocument(Job.IDDB -> job.mainID),
                  BSONDocument("$set"   -> BSONDocument(Job.STATUS -> job.status)))
      case None =>
        // There was no such job in the database, editing
        addJob(job)
     }
  }



  /**
    * @param job
    */
  def executeJob(job : Job): Unit = {
    updateJob(job.copy(status = JobState.Queued))
    val rootPath  = s"$jobPath$SEPARATOR${job.mainID.stringify}$SEPARATOR"
    // Create new Process instance of the runscript to run the tool
    val process = Process(job.scriptPath , new java.io.File(rootPath)).run()
    runningProcesses.put(job.mainID.stringify, process)
    // set job state to queued
  }



  /**
    * Deletes a Job from the Database and from the file system
    * @param job
    * @param userID
    */
  def deleteJob(job : Job, userID : BSONObjectID) = {

    //  Terminate running Process instance of the Job
    if (runningProcesses.contains(job.mainID.stringify)) {
      runningProcesses(job.mainID.stringify).destroy()
    }

    hashCollection.flatMap(_.remove(BSONDocument(JobHash.ID -> job.mainID)))
    jobDao.deleteJob(job.mainID.stringify) // remove deleted jobs from elasticsearch job and hash indices


    val jobIDFile = s"$jobPath$SEPARATOR${job.mainID.stringify}${SEPARATOR}jobIDCluster"
    val jobIDCluster = scala.io.Source.fromFile(jobIDFile).mkString
    if(job.status == JobState.Running || job.status == JobState.Queued) {
      // deleting job on sge
      s"qdel $jobIDCluster".!
      Logger.info("Deleted Job on SGE")
    }
    // set jobStatus to deleted
    UpdateJobStatus(job.mainID,JobState.Deleted)
    Logger.info(s"Updated job status of ${job.mainID.stringify} to Deleted")
  }


  def receive : Receive = {

    // Get a request to send the job list
    case FetchJobs(userID, mainIDs : List[BSONObjectID]) =>
      // Find all jobs related to the session ID
      val futureJobs = findJobs(BSONDocument(Job.IDDB ->
                                BSONDocument("$in" -> mainIDs)))

      // Collect the list and then create the reply
      futureJobs.foreach { jobList =>
        //println("Found " + jobList.length.toString + " Job[s]. Sending.")
        userManager ! SendJobList(userID, jobList)
    }


     // Reads parameters provided to the job from the job directory
    case Read(user : BSONObjectID, jobID : String) =>
      val replyTo = sender()
      findJob(BSONDocument(Job.JOBID -> jobID)).foreach {
        case Some(job) => // Job Owner must be linked with the Session ID
          job.ownerID match {
            case Some(ownerID) =>
              if (ownerID == user) // Retrieve the Job Files
                replyTo ! s"$jobPath$SEPARATOR${job.mainID.stringify}${SEPARATOR}params".toFile.list.map { f =>
                  f.name -> f.contentAsString
                }.toMap
              else // If jobID does not belong to the user
                replyTo ! PermissionDenied(user)
            case None =>
              replyTo ! s"$jobPath$SEPARATOR${job.mainID.stringify}${SEPARATOR}params".toFile.list.map { f =>
                f.name -> f.contentAsString
              }.toMap
          }
        case None => // If jobID is unknown
          replyTo ! JobIDUnknown(user)
      }


    // User Requests State of Job
    case JobInfo(user : BSONObjectID, jobID : String) =>
    // TODO Move this logic to the Controller
      val replyTo = sender()
        findJob(BSONDocument(Job.JOBID -> jobID)).foreach {
          case Some(job) => // Job Owner must be linked with the Session ID
            job.ownerID match {
              case Some(ownerID) =>
                if (ownerID == user) // Retrieve the Job Files
                  replyTo ! job
                else // If jobID does not belong to the user
                  replyTo ! PermissionDenied(user)
              case None =>
                replyTo ! job
            }
          case None => // If jobID is unknown
            replyTo ! JobIDUnknown(user)
        }

    case AddJob (userID : BSONObjectID, mainID : BSONObjectID) =>
      findJob(BSONDocument(Job.IDDB -> mainID)).foreach {
        case Some(job) =>
          userManager ! JobAdded(userID, job)
        case None =>
          // Job ID is unknown.
          userManager ! JobIDUnknown(userID)
      }

    //  User asks to delete Job
    case DeleteJob(userID : BSONObjectID, mainID : BSONObjectID) =>
      findJob(BSONDocument(Job.IDDB -> mainID)).foreach {
        case Some(job) =>
          job.ownerID match {
            case Some(ownerID) =>
              if (ownerID == userID) {
                deleteJob(job, userID)
              } else {
                userManager ! PermissionDenied(userID)
                Logger.info("Permission Denied")
              }
            case None =>
              // TODO decide on what to do with public jobs
              deleteJob(job, userID)
          }
        case None      =>
          // Job ID is unknown.
          userManager ! JobIDUnknown(userID)

          Logger.info("Unknown ID " + mainID.toString())
      }

    case UpdateJobStatus(jobID : BSONObjectID, status : JobState) =>
      findJob(BSONDocument(Job.IDDB -> jobID)).foreach {
        case Some(job) =>
          if (status == JobState.Done)
            runningProcesses.remove(job.mainID.stringify)
          updateJob(job.copy(status = status))
          userManager ! JobStateChanged(job, status)
          Logger.info("Successfully updated Job status " + jobID.toString + " to " + status)
        case None =>
          userManager ! JobIDUnknown(jobID)
          Logger.info("Unknown ID " + jobID.toString())
      }


    case StartJob(userID : BSONObjectID, mainID : BSONObjectID) =>
      findJob(BSONDocument(Job.IDDB -> mainID)).foreach{
        case Some(job) =>
          executeJob(job)
        case None =>
          userManager ! JobIDUnknown(userID)
      }

    // User asks to prepare new Job, might be directly executed (if start is true)
    case Prepare(user, jobID, mainID, toolName, params, start) =>
        val jobCreationTime = DateTime.now()
        val isPrivate       = params.getOrElse("private","") == "true"
        val ownerID         = if (isPrivate) Some(user.userID) else None
        val newJob = Job(mainID      = mainID,
                         jobType     = "",
                         parentID    = None,
                         jobID       = jobID.getOrElse(jobIDSource.next().toString), //TODO Refactor to name
                         ownerID     = ownerID,
                         status      = JobState.Submitted,
                         tool        = toolName,
                         statID      = "",
                         watchList   = Some(List(user.userID)),
                         runtime     = Some(""),
                         memory      = Some(0),
                         threads     = Some(0),
                         dateCreated = Some(jobCreationTime),
                         dateUpdated = Some(jobCreationTime),
                         dateViewed  = Some(jobCreationTime))

        // Add the job to the users watchlist
        modifyUser(BSONDocument(User.IDDB   -> user.userID),
                   BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> newJob.mainID)))

        // finally Add the job to the Database
        updateJob(newJob)

        // Interfaces with TEL to make a new job directory, returns the  path to the script which then
        // needs to be executed
        val rootPath = s"$jobPath$SEPARATOR${newJob.mainID.stringify}$SEPARATOR"

        val script = tel.init(toolName, params, rootPath)
        this.updateJob(newJob.copy(status = JobState.Prepared))


        // Write a JSON File with the job information to the JobDirectory
        s"$rootPath$jobJSONFileName".toFile.write(Json.toJson(newJob).toString())


        // create job checksum, using FNV-1, a non-cryptographic hashing algorithm
        // to guarantee the uniqueness of a job we should consider to optimize the algorithm and take following parameters:
        // job parameters, inputfile, mtime of the database
        // TODO currently taking all parameters (including Job ID(Name)) into the hash, may need to change this

        lazy val DB = params.getOrElse("standarddb","").toFile  // get hold of the database in use
        lazy val jobByteArray = params.toString().getBytes // convert params to hashable byte array

        lazy val jobHash = {
          params.get("standarddb") match {
            case None => JobHash( mainID = newJob.mainID,
                                  inputHash = FNV.hash64(jobByteArray).toString(), // TODO check the probability of collisions
                                  dbName = Some("none"), // field must exist so that elasticsearch can do a bool query on multiple fields
                                  dbMtime = Some("1970-01-01T00:00:00Z") ) //really weird bug in elasticsearch, "none" was not accepted when a timestamp-like string existed, so take unix epoch time

            case _ => JobHash( mainID = newJob.mainID,
                               inputHash = FNV.hash64(jobByteArray).toString(), // TODO check the probability of collisions
                               dbName = Some(DB.name),
                               dbMtime = Some(DB.lastModifiedTime.toString)
            )
          }
        }

        hashCollection.flatMap(_.insert(jobHash)) // insert hash into jobhashes collection. This insertion should stay here even for the price of having code duplication
        // in the tool controller because jobhashes should only get into the database when the job succeeds. TODO we need to check on each job with same signature if jobstate is 'done'.


        // Also Start Job if requested
        if(start) {
          executeJob(newJob)
        }
    }
}

object JobManager {

  /**
    * Incoming messages
    */
  // Prepare Job with new parameters or create new job with specified parameters for the given tool
  case class Prepare(user     : User,
                     jobID    : Option[String],
                     mainID   : BSONObjectID,
                     toolName : String,
                     params   : Map[String, String],
                     start    : Boolean)

  // When the JobManager was asked to update a Job status
  case class UpdateJobStatus(job : BSONObjectID, status : JobState)

  // Jobmanager is asked to find jobs
  case class FetchJobs(userID : BSONObjectID, mainIDs : List[BSONObjectID]) extends MessageWithUserID

  // Add a Job to a Users view
  case class AddJob(userID : BSONObjectID, mainID : BSONObjectID) extends MessageWithUserID

  // Delete Job Entirely
  case class DeleteJob(userID : BSONObjectID, mainID : BSONObjectID) extends MessageWithUserID

  // Start a Job that has been Prepared but not started
  case class StartJob(userID : BSONObjectID, mainID : BSONObjectID) extends MessageWithUserID
  /**
    * Outgoing messages
    */
  // Send the job list to the user
  case class SendJobList(userID : BSONObjectID, jobList : List[Job]) extends MessageWithUserID

  // Failure replies
  case class JobIDUnknown(userID : BSONObjectID) extends MessageWithUserID
  case class PermissionDenied(userID : BSONObjectID) extends MessageWithUserID
  case class FailDeleted(userID : BSONObjectID, job : Job) extends MessageWithUserID

  // Success replies
  case class AckDeleted(userID : BSONObjectID, job : Job) extends MessageWithUserID

  // Reads the parameters from a prepared job and provides them to the user
  case class Read(userID : BSONObjectID, jobID : String)

  // Publish changes to the JobState
  case class JobStateChanged(job : Job, state : JobState.JobState)

  // Ask for jobInfo (tool name and state)
  case class JobInfo(userID : BSONObjectID, jobID : String)
}
