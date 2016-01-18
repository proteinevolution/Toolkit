package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import play.api.libs.json.{JsValue, Json}



// TODO Currently the Websocket cannot do much

class WebSocketActor(uid: String, out: ActorRef) extends Actor with ActorLogging {

  /** The user actor subscribes at the JobActor on Startup */
  override def preStart() = {

    // Attach this Websocket to the corresponding user Actor
    UserManager() ! TellUser(uid, AttachWS(self))
  }


  def receive = LoggingReceive {


    /* Messages received from the UserActor and passed to the WebSocket
      */

    case UserJobStateChanged(newState, jobID)  =>

      out ! Json.obj("type" ->  "jobstate", "newState" -> newState.no, "jobid" -> jobID)
  }
}

object WebSocketActor {

  def props(uid: String)(out: ActorRef) = Props(new WebSocketActor(uid, out))
}
