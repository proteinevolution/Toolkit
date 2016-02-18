package actors


import actors.UserActor.{GetAllJobs, JobIDInvalid, JobStateChanged, AttachWS}
import actors.UserManager.GetUserActor
import actors.WebSocketActor.JobList
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import models.jobs.Job
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.{JsValue, Json}


object WebSocketActor {

  def props(uid : String, userManager : ActorRef)(out: ActorRef) = Props(new WebSocketActor(uid, userManager, out))
  case class JobList(list : Iterable[Job])
}


class WebSocketActor(uid: String, userManager : ActorRef, out: ActorRef)  extends Actor with ActorLogging {

  /** The user actor subscribes at the JobActor on Startup */
  override def preStart() = {

    // Attach this Websocket to the corresponding user Actor
    userManager ! AttachWS(uid, self)
  }
  implicit val timeout = Timeout(5.seconds)

  def receive = LoggingReceive {

    /*
    Messages received from the websocket and passed to the User
     */
    case js: JsValue =>

      (js \ "type").validate[String] map {

        case  "getJobs" =>
          Logger.info("WebSocket Actor Received message")
          (userManager ? GetUserActor(uid)).mapTo[ActorRef].map { userActor =>
            Logger.info("Send GetAllJobs to UserActor")
            userActor ! GetAllJobs
          }
      }

    case JobIDInvalid =>

      out ! Json.obj("type" -> "jobidinvalid")


    /* Messages received from the UserActor and passed to the WebSocket
      */
    case JobStateChanged(jobid, state) =>
      out ! Json.obj("type" -> "jobstate", "newState" -> state.no, "jobid" -> jobid)


    /* Passes the full list of jobs to the WebSocket
      */
    case JobList(joblist) =>

      val jobListObjs = for (job <- joblist) yield {
        Json.obj("t" -> job.toolname,
                  "s" -> job.state.no,
                  "i" -> job.id)
      }
      out ! Json.obj("type" -> "joblist", "jobs" -> jobListObjs)
  }
}
