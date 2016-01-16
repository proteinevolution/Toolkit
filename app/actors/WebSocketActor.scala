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

    // We want the Jobmanager to register this new user, before the websocket is not available
    //UserManager() ? SubscribeUser(uid)
  }


  def receive = LoggingReceive {

    // just a generic Interface of the Server to the WebSocket
    case Message(muid, msg) => out ! Json.obj("uid" -> muid, "msg" -> msg)

    // UserActor receives JSON data, most probably from a input form

    case js: JsValue =>
      (js \ "type").validate[String].get match {

        case "jobinit" =>

        // Fetch the details GET String from the JSON data
        // Prepare Working Directory in Job Manager and start immediately
        ///jobmanager ! JobSubmission((js \ "jobinit").validate[String].get, startJob = true)
      }

    /*
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
  }*/
  }
}

object WebSocketActor {

  def props(uid: String)(out: ActorRef) = Props(new WebSocketActor(uid, out))
}

