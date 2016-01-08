package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Terminated
import play.libs.Akka
import akka.actor.Props


/**
  * This actor is responsible for managing all Jobs that are currently
  * present in the Application. The Job Actor also knows about all the users that are registered.
  * All registered users are currently represented by a corresponding Actor ref of the User Actor.
  *
  */
class JobManager extends Actor with ActorLogging {

  var jobID = 0


  // set of all users that are under way
  var users = scala.collection.mutable.Set[ActorRef]()

  // Maps the JobID to the corresponding JobWorker Actor
  //var workers = scala.collection.mutable.Map[Long, ActorRef]()

  // Maps the jobID to the String identifier of the associated tool
  //private val toolMap = Map[Long, String]()


  def receive = LoggingReceive {

    /**
      *
      * Handles what the JobManager should do if he receives a new JobInit Request
     */
    case JobInit(toolname, details) =>

      log.info("LogManager tries to start a new Job")
      // TODO There might be several cases when a Job cannot be started properly. We must handle this case here
      val newWorker = context.actorOf(Props[JobWorker])
      jobID += 1
      context watch newWorker
      newWorker ! Start(toolname, details, jobID, sender)

      // inform the corresponding user actor about the status of the job initialization
      // Currently, only success encoded
      sender ! JobInitStatus(toolname, jobID, "success")


    case JobDone(userActor, toolname, details, jobID) =>

      userActor ! JobDone(userActor, toolname, details, jobID)

      log.info("JobManager received that Job is done")
      // we can terminate the sender
      context.stop(sender)


    /**
      * Subscribes new user to the System
      *
      */
    case Subscribe =>
      users += sender
      context watch sender

    case Terminated(user) => users -= user
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


