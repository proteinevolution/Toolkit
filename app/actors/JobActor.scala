package actors

import akka.actor.{Actor, Props}
import akka.event.LoggingReceive
import play.api.Logger

/**
  * Created by lzimmermann on 02.12.16.
  */


object JobActor {

  case object Delete

  def props(jobID : String, ownerUserID: String) = Props(new JobActor(jobID, ownerUserID))
}


class JobActor(val jobID: String, val ownerUserID: String) extends Actor {
  import JobActor._


  // Set of sessionIDs of all users that are subscribed to this Job
  private var subscribers = Set(ownerUserID)


  def receive = LoggingReceive {

    case Delete =>

      Logger.info(s"JobID: $jobID to be deleted")

  }
}
