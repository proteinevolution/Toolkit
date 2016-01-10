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
    // TODO The toolname must be decoded from the JSON string, Call must be adapted
      /*
    case js: JsValue =>
      (js \ "type").validate[String].get match {

        case "jobinit" =>
          // Get the toolname from the form
          jobmanager ! JobInit("foo", (js \ "jobinit").validate[String].get)
      }*/

    // Informs the user whether the Job was initialized Successfully
    case JobInitStatus(toolname, jobID, status) => out ! Json.obj("type" -> "JobInitStatus",
                                                                  "status" -> status,
                                                                  "jobid" -> jobID,
                                                                  "toolname" -> toolname)


    /* In this block we will handle several events the user might encounter */
    case JobDone(userActor, toolname, details, jobID) =>

      out ! Json.obj("type" -> "JobDone",
                    "jobid" -> jobID,
                    "toolname" -> toolname)

    case other => log.error("unhandled: " + other)
  }
}

object UserActor {

  def props(uid: String)(out: ActorRef) = Props(new UserActor(uid, JobManager(), out))
}
