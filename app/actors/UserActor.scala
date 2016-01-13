package actors

import akka.actor.{ActorLogging, Actor}
import akka.event.LoggingReceive

/**
  *  The User actor will represent each user who is present on the toolkit and
  *  encompass its possible interactions with the server.
  *
  * Created by lukas on 1/13/16.
  *
  */
class UserActor extends Actor with ActorLogging {



  def receive = LoggingReceive {


    null
  }
}
