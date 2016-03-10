package actors


import actors.UserActor.{JobIDInvalid, JobStateChanged, AttachWS}
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import play.api.libs.json.Json


/**
  * Actor that listens to the WebSocket and accepts messages from and passes messages to it.
  *
  */
object WebSocketActor {

  def props(userActor : ActorRef)(out: ActorRef) = Props(new WebSocketActor(userActor, out))
}

class WebSocketActor(userActor : ActorRef, out: ActorRef)  extends Actor with ActorLogging {

  // The user Actor subscribes itself to the corresponding userActor on startup
  override def preStart() = {

    // Attach this Websocket to the corresponding user Actor
    userActor ! AttachWS(self)
  }

  def receive = LoggingReceive {

    /*
     * Messages the user that there was a problem in handling the Job ID that was most recently provided
     */
    case JobIDInvalid => out ! Json.obj("type" -> "jobidinvalid")

    /*
     * Messages the user about a change in the Job status
     */
    case JobStateChanged(job_id, state) =>

      // Sends the message that a job state has changed over the WebSocket. This is probably the most important
      // Real-time notification in this application
      out ! Json.obj("type" -> "jobstate", "newState" -> state.no, "job_id" -> job_id)
  }
}
