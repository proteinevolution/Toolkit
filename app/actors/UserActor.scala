package actors
import javax.inject._

import actors.Worker._
import akka.actor._
import akka.event.LoggingReceive
import akka.util.Timeout
import models.database.{DBJobRef, DBJob}
import models.jobs._
import models.misc.RandomString
import play.api.Logger
import com.google.inject.assistedinject.Assisted
import scala.concurrent.duration._

/**
  *  The User actor will represent each user who is present on the toolkit and
  *  encompass its possible interactions with the server.
  *
  * Created by lukas on 1/13/16.
  *
  */

// A links just connects one output port to one input port
case class Link(out : Int, in : Int)

object UserActor {
  // All messages that the UserActor can actually receive
  // Job changed state
  case class JobStateChanged(job_id : String, state : JobState)

  // Start a job
  case class PrepWD(toolname : String, params : Map[String, String], startImmediate : Boolean, job_id_o : Option[String])

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
  case object UpdateJobs

  // User requested a suggestion
  case class AutoComplete(suggestion : String)

  // Socket attached / Starting socket session
  case class AttachWS(session_id : String, ws : ActorRef)

  trait Factory {
    def apply(session_id: String): Actor
  }
}

class UserActor @Inject() (@Named("worker") worker : ActorRef,
                           @Assisted session_id: String,
                           jobDB    : models.database.Jobs,
                           jobRefDB : models.database.JobReference) extends Actor with ActorLogging {

  val user_id : Long = 12345L   // TODO Implement User ID
  implicit val timeout = Timeout(5.seconds)

  import UserActor._

  // The websocket that is attached to the User
  var ws = None: Option[ActorRef]

  // The User Actor maps the job_id to the actual job instance
  val userJobs        = new collection.mutable.HashMap[String, UserJob]
  val databaseMapping = new collection.mutable.HashMap[String, DBJobRef]

  def receive = LoggingReceive {

    case AttachWS(_, ws_new) =>

      ws = Some(ws_new)
      context watch ws.get
      Logger.info("WebSocket attached successfully\n")


    // Job Preparation Routine for a new Job
    //  TODO The semantic of this message is not well defined, should work on that
    case PrepWD(toolname, params, startImmediate, job_id_o) =>

      // Determine the Job ID for the Job that was submitted
      val job_id : String = job_id_o match {
        // Job ID was selected by the User
        case Some(id) => id
        // Job ID was none, generate a random ID
        case None     => RandomString.randomNumString(7)
        //TODO: check whether this random id already exists in the db or make the userJobs Map entirely consistent with the Database
      }
      Logger.info("UserActor wants to prepare job directory for tool " + toolname + " with job_id " + job_id)

      val job   = UserJob(self, toolname, job_id, user_id, startImmediate)
      val dbJob = jobRefDB.update(DBJob(None, job_id, user_id, job.getState, job.toolname), session_id)

      userJobs.put(job_id, job)
      databaseMapping.put(job_id, dbJob)
      worker ! WPrepare(job, params)


    // Removes a Job (from the view and user actor as well as from the folder structure)
    case DeleteJob(job_id) =>
      val job = userJobs.remove(job_id).get    // Remove from User Model
      databaseMapping.remove(job_id)           // Remove from the
      worker ! WDelete(job)                    // Worker removes Directory

    // Removes a Job (from the view and from user actor)
    case ClearJob(job_id) =>
      Logger.info("Clear Actor")
      jobRefDB.delete(databaseMapping.remove(job_id).get)
      val job = userJobs.remove(job_id).get

    // Returns a Job for a given job_id
    case GetJob(job_id) =>  sender() ! userJobs.get(job_id).get

    case GetJobParams(job_id) => worker forward WRead(userJobs(job_id))

    // Updates all jobs from the database
    case UpdateJobs =>
      for (jobRef <- jobRefDB.get(session_id)) {
        val dbJob = jobDB.get(jobRef.main_id).get
        val job   = UserJob(self, dbJob.toolname, dbJob.job_id, dbJob.user_id, dbJob.job_state, true)
        userJobs.put(dbJob.job_id,job)
        databaseMapping.put(dbJob.job_id,jobRef)
      }

    // Returns all Jobs
    case GetAllJobs => sender() ! userJobs.values



    case AppendChildJob(parent_job_id, toolname, links) =>

      Logger.info("Append child job received")


      var new_job_id = None: Option[String]
      do {
        //TODO: check whether this random id already exists in the db or make the userJobs Map entirely consistent with the Database
        new_job_id = Some(RandomString.randomNumString(7))
      } while(userJobs contains new_job_id.get)

      val job = UserJob(self, toolname, new_job_id.get, user_id, false)  // TODO Start immediate not yet supported for child jobs
      userJobs.put(job.job_id, job)

      userJobs(parent_job_id).appendChild(job, links)


    case Convert(parent_job_id, child_job_id, links) =>

      Logger.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")

      worker ! WConvert(userJobs(parent_job_id), userJobs(child_job_id), links)


    // Connection was ended
    case Terminated(ws_new) =>  ws.get ! PoisonPill

    // Job status was changed
    case JobStateChanged(job_id, state) =>

      val userJob = userJobs.get(job_id).get

      // If the job changed to prepared and if it is set to start immediately, start the Job
      if(state == Prepared && userJob.startImmediate) {
        worker ! WStart(userJob)
      }

      // Forward Job state to Websocket
      ws.get ! JobStateChanged(job_id, state)

      // get the main ID
      val main_id = Some(databaseMapping.get(job_id).get.main_id)

      // update Job state in Persistence
      jobRefDB.update(DBJob(main_id, job_id, user_id, userJob.getState, userJob.toolname), session_id)

    case AutoComplete (suggestion : String) =>
      jobDB.findJobID(user_id, suggestion).headOption match {
        // Found something, return it to the user
        case Some(dbJob) => ws.get ! AutoComplete(dbJob.job_id)
        // Found nothing, do nothing.
        case None        =>
      }


    /* All of the remaining messages are just passed further to the WebSocket
    *  Currently: JobIDInvalid
    * */
    case m =>  ws.get ! m



  }
}



/*

val myActor = system.actorOf(Props[MyActor].withDispatcher("my-dispatcher"), name = "myactor2")

import akka.actor.{ Actor, Props, Terminated }

class WatchActor extends Actor {
val child = context.actorOf(Props.empty, "child")
context.watch(child) // <-- this is the only call needed for registration
var lastSender = system.deadLetters

def receive = {
  case "kill" =>
    context.stop(child); lastSender = sender()
  case Terminated(`child`) => lastSender ! "finished"
}
}
 */

























