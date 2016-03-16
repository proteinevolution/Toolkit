package actors


import actors.UserActor.{JobIDInvalid, JobStateChanged, AttachWS}
import actors.UserActor._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import models.database.DBJob
import play.api.libs.json.{JsValue, JsArray, Json}


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

  def createJobObjList (dbJobSeq : Seq[DBJob]) : JsArray = {
    JsArray(for (dbJob <- dbJobSeq) yield {
      Json.obj("t" -> dbJob.toolname,
               "s" -> dbJob.job_state.no,
               "i" -> dbJob.job_id)
    })
  }

  def receive = LoggingReceive {


    case js: JsValue =>

      (js \ "type").validate[String] map {

        case "getSuggestion" =>
            (js \ "query").validate[String].map {
              query => userActor ! AutoComplete(query)
            }
      }

    // Messages the user that there was a problem in handling the Job ID
    case JobIDInvalid  => out ! Json.obj("type" -> "jobidinvalid")

    // Updates the User about updating their joblist
    case UpdateJobList => out ! Json.obj("type" -> "updatejoblist")

    /*
     * Messages the user about a change in the Job status
     */
    case JobStateChanged(job_id, state) =>

      // Sends the message that a job state has changed over the WebSocket. This is probably the most important
      // Real-time notification in this application
      out ! Json.obj("type" -> "jobstate", "newState" -> state.no, "job_id" -> job_id)

    case AutoCompleteSend (suggestions : Seq[DBJob]) =>
      out ! Json.obj("type" -> "autocomplete", "suggestions" -> createJobObjList(suggestions))
  }
}
