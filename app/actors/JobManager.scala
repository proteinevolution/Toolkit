package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.Props
import akka.routing.RoundRobinPool
import play.libs.Akka
import play.api.Logger


/**
  * This actor is responsible for managing all Jobs that are currently
  * present in the Application. The Job Actor also knows about all the users that are registered.
  * All registered users are currently represented by a corresponding Actor ref of the User Actor.
  *
  */
class JobManager extends Actor with ActorLogging {


  // set of workers to be used
  val workerActors = Akka.system().actorOf(Props[Worker].withRouter(RoundRobinPool(4)), name = "WorkerActors")
  var jobID = 0



  def receive = LoggingReceive {


    /**
      * Prepares a new working directory for processing the job
      */
    case PrepWD(paramMap, toolname, uid) =>

      jobID += 1

      Logger.info("Job Manager wants to prepare working directory for Job\n")
      workerActors ! Prepare(paramMap, jobID, toolname, uid)


    case PrepWDDone(jobID_l) =>

      Logger.info("Job Manager got to know that the working directory for Job " + jobID_l + " is now ready")
      workerActors ! Start(jobID_l)


    case JobDone(jobID_l) =>

        null //TODO Implement me
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


