package actors

import akka.actor._
import akka.event.LoggingReceive
import play.api.Logger
/**
  *  The User actor will represent each user who is present on the toolkit and
  *  encompass its possible interactions with the server.
  *
  * Created by lukas on 1/13/16.
  *
  */
class UserActor(uid: String) extends Actor with ActorLogging {

  var ws: ActorRef = null


  def receive = LoggingReceive {


    case AttachWS(ws_new) =>

      this.ws = ws_new
      context watch ws
      Logger.info("WebSocket atached successfully\n")



    case Terminated(ws_new) =>

      ws = null
  }





}
object UserActor {

  def props(uid: String) = Props(new UserActor(uid))
}
