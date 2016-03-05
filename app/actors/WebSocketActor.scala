package actors


import actors.UserActor.{GetAllJobs, JobIDInvalid, JobStateChanged, AttachWS}
import actors.UserManager.GetUserActor
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.materializer


import play.api.libs.json.{JsValue, Json}


object WebSocketActor {

  def props(session_id : String, userManager : ActorRef)(out: ActorRef) = Props(new WebSocketActor(session_id, userManager, out))
}


class WebSocketActor(session_id: String, userManager : ActorRef, out: ActorRef)  extends Actor with ActorLogging {

  implicit val timeout = Timeout(5.seconds)

  /** The user actor subscribes at the JobActor on Startup */
  override def preStart() = {

    // Attach this Websocket to the corresponding user Actor
    userManager ! AttachWS(session_id, self)
  }

  def receive = LoggingReceive {

    /*
     * Messages received from the websocket and passed to the User
     */
    case js: JsValue =>

      (js \ "type").validate[String] map {

        case  "getJobs" =>
          Logger.info("WebSocket Actor Received message")
          (userManager ? GetUserActor(session_id)).mapTo[ActorRef].map { userActor =>
            Logger.info("Send GetAllJobs to UserActor")
            userActor ! GetAllJobs
          }
      }

    /*
     * Messages the user that there was a problem in handling the Job ID
     */
    case JobIDInvalid => out ! Json.obj("type" -> "jobidinvalid")

    /*
     * Messages the user about a change in the Job status
     */
    case JobStateChanged(job_id, state) =>
      Logger.info("WebSocketActor received: JobState Changed")
      out ! Json.obj("type" -> "jobstate", "newState" -> state.no, "job_id" -> job_id)
  }
}
