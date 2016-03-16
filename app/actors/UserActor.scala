package actors

import javax.inject._
import actors.Worker._
import akka.actor._
import akka.event.LoggingReceive
import models.database.{DBJobRef, DBJob}
import models.jobs._
import models.misc.RandomString
import play.api.Logger
import com.google.inject.assistedinject.Assisted

/**
  *  The User actor will represent each user who is currently present on the toolkit and
  *  describe possible interaction with the web application. If a new session is created, a new UserActor
  *  is created as well.
  *
  * Created by lukas on 1/13/16.
  */
object UserActor {
  /*
   *  All messages that the UserActor can actually receive are listed here.
   */

  // Job changed state
  case class JobStateChanged(job_id : String, state : JobState)

  case class PrepWD(toolname : String, params : Map[String, String], startImmediate : Boolean, job_id_o : Option[String])

  case class UpdateWD(job_id : String, params : Map[String, String], startImmediate : Boolean)


  // Job ID was Invalid
  case object JobIDInvalid

  // Requested a Job with Job ID
  case class GetJob(jobID : String)

  // Requested a list of all Job IDs
  case object GetAllJobs

  case class GetJobParams(job_id : String)

  case class GetJobView(job_id : String)

  case class DeleteJob(job_id : String)

  case class AppendChildJob(parent_job_id : String, toolname : String, links : Seq[Link])

  case class ClearJob(job_id : String)

  case class Convert(parent_job_id : String, child_job_id : String, links : Seq[Link])

  // Load jobs from the database
  case object LoadJobsFromDB

  // Tells the User to reload their joblist
  case object UpdateJobList

  // Loads a single job from the database and loads it into the user actor
  case class LoadJob(job_id : String)

  // Attach WebSocket Actor to UserActor
  case class AttachWS(ws : ActorRef)

  case class UpdateWDDone(userJob : UserJob)


  // User requested a suggestion
  case class AutoComplete(suggestion : String)
  case class AutoCompleteSend (suggestions : Seq[DBJob])

  trait Factory {
    def apply(session_id: String): Actor
  }
}

class UserActor @Inject() (@Named("worker") worker : ActorRef,
                           @Assisted session_id: String,
                           jobDB    : models.database.Jobs,
                           jobRefDB : models.database.JobReference) extends Actor with ActorLogging {

  val user_id : Long = 12345L   // TODO Implement User ID

  import UserActor._

  // The websocket that is attached to the User
  var ws = None: Option[ActorRef]

  // The User Actor maps the job_id to the actual job instance, represented as UserJob
  val userJobs        = new collection.mutable.HashMap[String, UserJob]
  val databaseMapping = new collection.mutable.HashMap[String, DBJobRef]

  /**
    * Is starting when the user actor is initialized
    */
  override def preStart() = {
    Logger.info("updating jobs from the database")
    self ! LoadJobsFromDB
  }

  /**
    * Adds a Job to the user using a Database entry
 *
    * @param dbJob database entry of the job
    */
  def addJob(dbJob : DBJob) = {
    val job    = UserJob(self, dbJob.toolname, dbJob.job_id, dbJob.user_id, dbJob.job_state, true)
    val jobRef = jobRefDB.update(dbJob, session_id)
    userJobs.put(dbJob.job_id,job)
    databaseMapping.put(dbJob.job_id,jobRef)
  }

  /**
    * Checks if a given job_id is already used for a different job
 *
    * @param job_id_o selected job_id
    * @return
    */
  def checkJobID (job_id_o : Option[String]) : String = {
    // Determine the Job ID for the Job that was submitted
    var job_id : String = job_id_o.getOrElse(RandomString.randomNumString(7))
    while (jobDB.get(user_id, job_id).nonEmpty) {
      job_id = RandomString.randomNumString(7)
    }
    job_id
  }

  /**
    * Incoming actor message handler
    */
  def receive = LoggingReceive {

    case AttachWS(ws_new) =>

      ws = Some(ws_new)
      context watch ws.get   // .get is ok here, since it just got initalized with Some(ws_new)
      // Websocket attached, tell user to update their jobslist
      ws.get ! UpdateJobList // .get is ok
      Logger.info("WebSocket attached successfully")


    // Job Preparation Routine for a new Job
    case PrepWD(toolname, params, startImmediate, job_id_o) =>

      // Determine the Job ID for the Job that was submitted
      val job_id : String = checkJobID(job_id_o)
      Logger.info("UserActor wants to prepare job directory for tool " + toolname + " with job_id " + job_id)


      // Create a new Job instance
      val job = UserJob(self, toolname, job_id, user_id, Submitted, startImmediate)

      // This is a new Job, so we have to make the status *Submitted explicit*
      job.changeState(Submitted)

      // Make changes to the UserActor Model
      userJobs.put(job_id, job)

      // Put the new job into the Database Mapping
      databaseMapping.put(job_id, jobRefDB.update(DBJob(None, job_id, user_id, job.getState, job.toolname), session_id))


      worker ! WPrepare(job, params)



    //  Updates the Job directory of a provided job with a new set of parameters
    case UpdateWD(job_id,  params, startImmediate) =>
      Logger.info("User Actor was asked to update the working directory")
      val userJob =  userJobs(job_id)
      userJob.startImmediate = startImmediate
      worker ! WUpdate(userJobs(job_id), params)


    case UpdateWDDone(userJob) =>

      // If the Job state is prepared and we want to start the job, then start
      if(userJob.getState == Prepared && userJob.startImmediate) {

        userJob.changeState(Queued)
        worker ! WStart(userJob)
      }


    // Removes a Job completely
    case DeleteJob(job_id) =>

      if(userJobs.contains(job_id)) {

        val job = userJobs.remove(job_id).get // Remove from User Model, there should not exist a reference to that job anymore
        job.destroy() // Tells the job that is was destroyed
        databaseMapping.remove(job_id) // Remove job from the relation database mapping
        worker ! WDelete(job) // Worker removes Directory
      }

    // Removes the job from the UserActor, but keep it in the job database
    case ClearJob(job_id) =>

      val dbJobOption = databaseMapping.remove(job_id)
      dbJobOption match {
        case Some(dbJob) => jobRefDB.delete(dbJob)
                            userJobs.remove(job_id).get
        case None        =>
      }

    // Returns a Job for a given job_id
    // TODO Handle the case that the user requests a job which does not belong to him
    case GetJob(job_id) => sender() ! userJobs.get(job_id)

    // Read the parameter map from the job directory
    case GetJobParams(job_id) => worker forward WRead(userJobs(job_id))


    // Asks the user actor to load jobs from the database in the JobModel
    case LoadJobsFromDB =>
      for (jobRef <- jobRefDB.get(session_id)) {
        val dbJob_o = jobDB.get(jobRef.main_id)
        dbJob_o match {
          // job with the main_id exists, add the job
          case Some(dbJob) => addJob(dbJob)
          // delete the jobRef from the DB, as the main_id is no longer there
          case None => jobRefDB.delete(jobRef)
        }
      }

    // Asks the user actor to load a single job from the database in the JobModel
    case LoadJob (job_id : String) =>
      val dbJob_o = jobDB.get(user_id, job_id).headOption
      dbJob_o match {
        case Some(dbJob) => addJob(dbJob)

        case None => ws.get ! JobIDInvalid
      }

    // Returns all Jobs
    case GetAllJobs => sender() ! userJobs.values

    /* Appends a new Job to one parent job */
    case AppendChildJob(parent_job_id, toolname, links) =>

      // Load the Parent job from the Database if it is not in the UserActor
      if(!userJobs.contains(parent_job_id)) {

        self ! LoadJob(parent_job_id)
      }
      // Generate new Job ID
      val job_id : String = checkJobID(None)
      val job = UserJob(self, toolname, job_id, user_id, Submitted,  false)

      // This is a new Job, so we have to make the Job state *Submitted* explicit
      job.changeState(Submitted)

      // Put the new job into the Database Mapping
      databaseMapping.put(job.job_id, jobRefDB.update(DBJob(None, job.job_id, user_id, job.getState, job.toolname), session_id))
      userJobs.put(job.job_id, job)

      userJobs(parent_job_id).appendChild(job, links)


    case Convert(parent_job_id, child_job_id, links) =>
      worker ! WConvert(userJobs(parent_job_id), userJobs(child_job_id), links)


    // Connection To the WebSocket was ended
    case Terminated(ws_new) =>  ws.get ! PoisonPill

    // UserActor got to know that the job state has changed
    case JobStateChanged(job_id, state) =>

      if(userJobs.contains(job_id)) {

        val userJob = userJobs(job_id)

        // Forward Job state to Websocket
        ws.get ! JobStateChanged(job_id, state)

        // get the main ID
        val main_id_o = Some(databaseMapping.get(job_id).get.main_id)

        // update Job state in Persistence
        jobRefDB.update(DBJob(main_id_o, job_id, user_id, userJob.getState, userJob.toolname), session_id)
      }

    case AutoComplete (suggestion : String) =>
      val dbJobSeq = jobDB.suggestJobID(user_id, suggestion)
      // Found something, return it to the user
      ws.get ! AutoCompleteSend(dbJobSeq)

    /* All of the remaining messages are just passed further to the WebSocket
    *  Currently: JobIDInvalid
    * */
    case m =>  ws.get ! m

  }
}
// A links just connects one output port to one input port
case class Link(out : Int, in : Int)





















