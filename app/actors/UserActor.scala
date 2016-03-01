package actors
import javax.inject._

import actors.Worker.{WDelete, WRead, WStart, WPrepare}
import akka.actor._
import akka.event.LoggingReceive
import akka.util.Timeout
import models.jobs._
import play.api.Logger
import com.google.inject.assistedinject.Assisted
import scala.concurrent.duration._
import scala.annotation.tailrec

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


  // Socket attached / Starting socket session
  case class AttachWS(user_id : Long, ws : ActorRef)

  trait Factory {
    def apply(user_id: Long): Actor
  }
}

class UserActor @Inject() (@Named("worker") worker : ActorRef,
                           @Assisted user_id: Long,
                           jobDB : models.database.Jobs) extends Actor with ActorLogging {

  implicit val timeout = Timeout(5.seconds)

  import UserActor._

  // The websocket that is attached to the User
  var ws: ActorRef = null

  // The User Actor maps the job_id to the actual job instance
  val userJobs = new collection.mutable.HashMap[String, UserJob]

  val job_id_generator = scala.util.Random


  def randomAlphaNumericString(length: Int): String = {
    val chars = ('0' to '9')
    randomStringFromCharList(length, chars)
  }

  def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
    val sb = new StringBuilder
    for (i <- 1 to length) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }



  def receive = LoggingReceive {

    case AttachWS(_, ws_new) =>

      ws = ws_new
      context watch ws
      Logger.info("WebSocket attached successfully\n")


     // Job Preparation Routine for a new Job
    case PrepWD(toolname, params, startImmediate, job_id_o) =>

      // Determine the Job ID for the Job that was submitted
      val job_id : String = job_id_o match {
        // Job ID was selected by the User
        case Some(id) => id
        // Job ID was none, generate a random ID
        case None =>

          var new_job_id : String = null
          do {
            new_job_id = randomAlphaNumericString(7: Int) //TODO: check whether this random id already exists in the db
          } while(userJobs contains new_job_id)

          new_job_id
      }
      Logger.info("UserActor wants to prepare job directory for tool " + toolname + " with job_id " + job_id)

      if(userJobs contains job_id) {

        // User has tried to submit same job_id twice
        self ! JobIDInvalid

      } else {

          val job = UserJob(self, toolname, PartiallyPrepared, job_id, user_id, startImmediate)
          userJobs.put(job.job_id, job)
          jobDB.add(DBJob(job.job_id, user_id, toolname))

          worker ! WPrepare(job, params)
      }


    case DeleteJob(job_id) =>

      val job = userJobs.remove(job_id).get    // Remove from User Model
      worker ! WDelete(job)                    // Worker removes Directory


    // Returns a Job for a given job_id
    case GetJob(job_id) =>  sender() ! userJobs.get(job_id).get

    case GetJobParams(job_id) => worker forward WRead(userJobs(job_id))

    // Returns all Jobs
    case GetAllJobs => sender() ! userJobs.values


    // Connection was ended
    case Terminated(ws_new) =>  ws = null

    // Job status was changed
    case m @ JobStateChanged(job_id, state) =>

      val userJob = userJobs.get(job_id).get

      // If the job changed to prepared and if it is set to start immediately, start the Job
      if(state == Prepared && userJob.startImmediate) {

        worker ! WStart(userJob)
      }

      // Forward Job state to Websocket
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

























