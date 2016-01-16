package actors

import akka.actor.{Props, ActorRef, ActorLogging, Actor}
import akka.event.LoggingReceive

/**
  *  The User actor will represent each user who is present on the toolkit and
  *  encompass its possible interactions with the server.
  *
  * Created by lukas on 1/13/16.
  *
  */
class UserActor(uid: String, ws: ActorRef) extends Actor with ActorLogging {


  def receive = LoggingReceive {

    null
  }

}
object UserActor {

  def props(uid: String, ws: ActorRef) = Props(new UserActor(uid, ws))
}
