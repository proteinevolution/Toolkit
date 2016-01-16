package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import play.api.libs.json.{JsValue, Json}



// TODO Currently the Websocket cannot do much

class WebSocketActor(uid: String, out: ActorRef) extends Actor with ActorLogging {

  //val user: ActorRef

  /** The user actor subscribes at the JobActor on Startup */
  override def preStart() = {

    // Attach this Websocket to the corresponding user Actor
    UserManager() ! TellUser(uid, AttachWS(self))
  }


  def receive = LoggingReceive {

    // UserActor receives JSON data, most probably from a input form

    case js: JsValue =>
      (js \ "type").validate[String].get match {

        case "jobinit" =>

        // Fetch the details GET String from the JSON data
        // Prepare Working Directory in Job Manager and start immediately
        ///jobmanager ! JobSubmission((js \ "jobinit").validate[String].get, startJob = true)
      }

  }
}

object WebSocketActor {

  def props(uid: String)(out: ActorRef) = Props(new WebSocketActor(uid, out))
}
