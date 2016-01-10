package actors

import akka.actor.{ActorLogging, Actor}
import language.postfixOps

/**
  * Class Intended to take care for the execution of one particular Job
  *
  * Created by lukas on 1/8/16.
  */
class JobWorker extends Actor with ActorLogging {

  def receive = {

    /**
      * Tell the worker to start the new Job with the details
      */
    case Start(jobID) =>






      //////////////////////////////////////////////////////////////

      // Job execution is done
      //sender ! JobDone(userActor, toolname, details, jobID)
  }
}
