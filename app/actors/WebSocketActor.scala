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
import models.jobs.UserJob
import play.api.Logger
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.json.{JsValue, Json}


object WebSocketActor {

  def props(user_id : Long, userManager : ActorRef)(out: ActorRef) = Props(new WebSocketActor(user_id, userManager, out))
  case class JobList(list : Iterable[UserJob])
}


class WebSocketActor(user_id: Long, userManager : ActorRef, out: ActorRef)  extends Actor with ActorLogging {

  implicit val timeout = Timeout(5.seconds)

  /** The user actor subscribes at the JobActor on Startup */
  override def preStart() = {

    // Attach this Websocket to the corresponding user Actor
    userManager ! AttachWS(user_id, self)
  }


  def receive = LoggingReceive {

    /*
    Messages received from the websocket and passed to the User
     */
    case js: JsValue =>

      (js \ "type").validate[String] map {

        case  "getJobs" =>
          Logger.info("WebSocket Actor Received message")
          (userManager ? GetUserActor(user_id)).mapTo[ActorRef].map { userActor =>
            Logger.info("Send GetAllJobs to UserActor")
            userActor ! GetAllJobs
          }
      }

    case JobIDInvalid => out ! Json.obj("type" -> "jobidinvalid")

    case JobStateChanged(job_id, state) =>
      Logger.info("WebSocketActor received: JobState Changed")
      out ! Json.obj("type" -> "jobstate", "newState" -> state.no, "job_id" -> job_id)


    case JobList(joblist) =>

      val jobListObjs = for (job <- joblist) yield {
        Json.obj("t" -> job.toolname,
                  "s" -> job.getState.no,
                  "i" -> job.job_id)
      }
      out ! Json.obj("type" -> "joblist", "jobs" -> jobListObjs)
  }
}
