package actors

import actors.JobManager._
import actors.UserManager.{UserDisconnect, UserConnect}
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import models.database.{User, Job}
import play.api.libs.json.{JsValue, Json}
import play.api.Logger
import reactivemongo.bson.BSONObjectID

/**
  * Actor that listens to the WebSocket and accepts messages from and passes messages to it.
  *
  */
object WebSocketActor {
  def props(user : User, jobManager : ActorRef)(out: ActorRef) = {
    Props(new WebSocketActor(user.userID, jobManager, out))
  }
}

private final class WebSocketActor(userID : BSONObjectID, userManager : ActorRef, out: ActorRef)  extends Actor with ActorLogging {
  override def preStart =
    // Connect to JobManager via Session ID
    userManager ! UserConnect(userID)

  override def postStop =
    // User Disconnected
    userManager ! UserDisconnect(userID)

  def receive = LoggingReceive {

    case js: JsValue =>
      (js \ "type").validate[String].foreach {
        // User requests the job list for the widget
        case "GetJobList" =>
          userManager ! GetJobList(userID)

        // connection test case
        case "Ping"       =>
          Logger.info("PING!")
        case _            =>
          Logger.error("Undefined Message: " + js.toString())
      }

    // Messages the user that there was a problem in handling the Job ID
    case JobIDUnknown =>
      out ! Json.obj("type" -> "JobIDUnknown")

    // Messages the user about a change in the Job status
    case JobStateChanged(job, state) =>
      out ! Json.obj("type"     -> "UpdateJob",
                     "job_id"   -> job.jobID,
                     "state"    -> state.no,
                     "toolname" -> job.tool)

    // Sends the job list to the user
    case SendJobList(userID : BSONObjectID, jobList : List[Job]) =>
      out ! Json.obj("type" -> "JobList",
                     "list" -> jobList.map(job =>
                        Json.obj("job_id"   -> job.jobID,
                                 "state"    -> job.status.no,
                                 "toolname" -> job.tool)))
  }
}
