package actors

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import play.api.libs.json.Json
import play.api.libs.json.JsValue

class UserActor(uid: String, jobmanager: ActorRef, out: ActorRef) extends Actor with ActorLogging {

  /** The user actor subscribes at the JobActor on Startup */
  override def preStart() = {
    log.info("User Actor tries to subscribe at the JobManager")
    JobManager() ! Subscribe
  }

  def receive = LoggingReceive {

    // just a generic Interface of the Server to the WebSocket
    case Message(muid, msg)  => out ! Json.obj("uid" -> muid, "msg" -> msg)

    // UserActor receives JSON data, most probably from a input form
    // TODO The toolname must be decoded from the JSON string
    case js: JsValue => (js \ "jobinit").validate[String] map { jobmanager ! JobInit(uid, "foo", _) }

    case other => log.error("unhandled: " + other)
  }
}

object UserActor {

  def props(uid: String)(out: ActorRef) = Props(new UserActor(uid, JobManager(), out))
}
