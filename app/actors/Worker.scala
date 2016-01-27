package actors


import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import models._
import play.api.{Play, Logger}



/**
  *
  * Created by lukas on 1/16/16.
  */


object Worker {

  case class WPrepare(job : SuppliedJob)
  case class WStart(jobID: String)
}


class Worker extends Actor with ActorLogging {

  import actors.Worker._


  def receive = LoggingReceive {

    case WPrepare(sjob) =>

     Logger.info("[Worker](WPrepare) for job " + sjob.id)


    case WStart(jobID) =>

      Logger.info("[Worker](WStart) for job " + jobID)


  }
}