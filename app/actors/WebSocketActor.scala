package actors


import actors.UserActor.{JobIDInvalid, JobStateChanged, AttachWS}
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import play.api.libs.json.{JsValue, Json}


class WebSocketActor(uid: String, userManager : ActorRef, out: ActorRef)  extends Actor with ActorLogging {

  /** The user actor subscribes at the JobActor on Startup */
  override def preStart() = {

    // Attach this Websocket to the corresponding user Actor
    userManager ! AttachWS(uid, self)
  }


  def receive = LoggingReceive {

    /*
    Messages received from the websocket and passed to the User
     */
    case js : JsValue => // TODO


    case JobIDInvalid =>

      out ! Json.obj("type" ->  "jobidinvalid")


    /* Messages received from the UserActor and passed to the WebSocket
      */
    case JobStateChanged(jobid, state)  =>

      out ! Json.obj("type" ->  "jobstate", "newState" -> state.no, "jobid" -> jobid)
  }
}

object WebSocketActor {

  def props(uid : String, userManager : ActorRef)(out: ActorRef) = Props(new WebSocketActor(uid, userManager, out))
}
