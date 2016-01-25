package actors

import java.io.File

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.Props
import akka.routing.RoundRobinPool
import play.api.Play._
import play.libs.Akka
import play.api.{Play, Logger}


/**
  * This actor is responsible for managing all Jobs that are currently
  * present in the Application. The Job Actor also knows about all the users that are registered.
  * All registered users are currently represented by a corresponding Actor ref of the User Actor.
  *
  */
class JobManager extends Actor with ActorLogging {


  // set of workers to be used
  val workerActors = Akka.system().actorOf(Props[Worker].withRouter(RoundRobinPool(4)), name = "WorkerActors")
  var jobID : Long = 0
  val path = s"${Play.application.path}${current.configuration.getString("job_path").get}${File.separator}"


  /* The JobManager determines which JobIDs are available */
  override def preStart() = {

    try {
      jobID = helpers.FileAccess.listDirectory(path).map(_.toLong).max
    }
    catch {
      case _ => jobID = 0
    }

  }


  def receive = LoggingReceive {


    /*
      * Prepares a new working directory for processing the job
      */
    case PrepWD(paramMap, toolname, uid) =>

      jobID += 1

      Logger.info("Job Manager wants to prepare working directory for Job\n")

      sender ! UserJobStateChanged(models.Job.instance(toolname, models.Running), jobID)
      workerActors ! Prepare(paramMap, jobID, toolname, uid)


    /*
     * Job Manager was told that the Preparation of the Working directory for the job is done
     */
    case PrepWDDone(jobID_l) =>

      Logger.info("Job Manager got to know that the working directory for Job " + jobID_l + " is now ready")
      workerActors ! Start(jobID_l)


    /*
     * Job Manager was told that the Job has been finished executing
     */
    case JobDone(jobID_l, exitCode, uid, toolname) =>

      Logger.info("JobManager was informed that Job is done")

      val status = exitCode match {

        case 0 => models.Done
        case _ => models.Error
      }

      // TODO Implement cache for UserActor
      UserManager() ! TellUser(uid, UserJobStateChanged(models.Job.instance(toolname, status), jobID))
  }
}

object JobManager {

  lazy val board = Akka.system().actorOf(Props[JobManager])

  def apply() = board
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


