package actors
import javax.inject._

import actors.Worker.{WStart, WPrepare}
import akka.actor._
import akka.event.LoggingReceive
import models.jobs._
import play.api.Logger
import com.google.inject.assistedinject.Assisted

/**
  *  The User actor will represent each user who is present on the toolkit and
  *  encompass its possible interactions with the server.
  *
  * Created by lukas on 1/13/16.
  *
  */

object UserActor {
  // All messages that the UserActor can actually receive
  // Job changed state
  case class JobStateChanged(job_id : String, state : JobState)

  // Start a job
  case class PrepWD(toolname : String, params : Map[String, String], startImmediately : Boolean, job_id_o : Option[String])

  // Job has been prepared
  case class PrepWDDone(job : UserJob)

  // Job ID was Invalid
  case object JobIDInvalid

  // Requested a Job with Job ID
  case class GetJob(jobID : String)

  // Requested a list of all Job IDs
  case object GetAllJobs

  // Socket attached / Starting socket session
  case class AttachWS(user_id : Long, ws : ActorRef)

  trait Factory {
    def apply(user_id: Long): Actor
  }
}


class UserActor @Inject() (@Named("worker") worker : ActorRef,
                           @Assisted user_id: Long,
                           jobDB : models.database.Jobs)
                  extends Actor with ActorLogging {

  import UserActor._

  // The websocket that is attached to the User
  var ws: ActorRef = null

  // The User Actor maps the job_id to the actual job instance
  val userJobs = new collection.mutable.HashMap[String, UserJob]

  val job_id_generator = scala.util.Random


  def receive = LoggingReceive {

    case AttachWS(_, ws_new) =>

      ws = ws_new
      context watch ws
      Logger.info("WebSocket attached successfully\n")


     // Job Preparation Routine
    case PrepWD(toolname, params, startImmediately, job_id_o) =>

      // Determine the Job ID for the Job that was submitted
      val job_id : String = job_id_o match {
        // Job ID was selected by the User
        case Some(id) => id
        // Job ID was none, generate a random ID
        case None =>

          var new_job_id : String = null
          do {
            new_job_id = job_id_generator.nextInt(10000).toString
          } while(userJobs contains new_job_id)

          new_job_id
      }
      Logger.info("UserActor wants to prepare job directory for tool " + toolname + " with job_id " + job_id)

      if(userJobs contains job_id) {

        // User has tried to submit same job_id twice
        self ! JobIDInvalid

      } else {

        // User Actor has to wait until Job has entered the Database
        val job = UserJob(self, toolname, PartiallyPrepared, job_id, user_id)

        userJobs.put(job.job_id, job)
        jobDB.add(DBJob(job.job_id, user_id, toolname))

        worker ! WPrepare(job, params)
      }

    // Job Dir has been prepared successfully
    case PrepWDDone(job) =>

      Logger.info("[UserActor] Job with job_id " + job.job_id + " was prepared successfully")
      worker ! WStart(job)

    // Returns a Job for a given ID
    case GetJob(job_id) =>  sender() ! userJobs.get(job_id).get

    // Returns all Jobs
    case GetAllJobs => sender() ! userJobs.values

    // Connection was ended
    case Terminated(ws_new) =>  ws = null

    // Job status was changed
    case m @ JobStateChanged(job_id, state) =>
      ws ! m

      // TODO update Job state in Persistence




    /* All of the remaining messages are just passed further to the WebSocket
    *  Currently: JobIDInvalid
    * */
    case m =>  ws ! m
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

























