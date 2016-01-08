package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Terminated
import models.JobResult
import play.libs.Akka
import akka.actor.Props
import scala.concurrent.Future


/**
  * This actor is responsible for managing all Jobs that are currently
  * present in the Application. The Job Actor also knows about all the users that are registered.
  * All registered users are currently represented by a corresponding Actor ref of the User Actor.
  *
  */
class JobManager extends Actor with ActorLogging {

  // keeps track of all the users registered in the Application
  var users = Set[ActorRef]()

  // maps jobID to the Future Object which holds the Job Result
  private val jobMap = Map[Long, Future[JobResult]]()

  // Maps the jobID to the String identifier of the associated tool
  private val toolMap = Map[Long, String]()




  def receive = LoggingReceive {

    /**
      * Handels what the JobManager should do if he receives a new JobInit Request
     */
    case JobInit(uuid, toolname, details) => {




    }

    case Subscribe => {
      users += sender
      context watch sender
    }
    case Terminated(user) => users -= user
  }
}

object JobManager {

  lazy val board = Akka.system().actorOf(Props[JobManager])
  def apply() = board
}
