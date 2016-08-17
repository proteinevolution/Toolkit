package actors


import actors.JobManager._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import models.database.{User, Job}
import play.api.libs.json.{JsValue, Json}
import play.api.Logger

/**
  * Actor that listens to the WebSocket and accepts messages from and passes messages to it.
  *
  */
object WebSocketActor {
  // TODO possibly use this object for session storing as the websockets are the first to show if a user is on / off
  def props(user : User, jobManager : ActorRef)(out: ActorRef) = Props(new WebSocketActor(user, jobManager, out))
}

private final class WebSocketActor(user : User, jobManager : ActorRef, out: ActorRef)  extends Actor with ActorLogging {
  //TODO Warning: user is not stable and only the ID is used here
  override def preStart =
    // Connect to JobManager via Session ID
    jobManager ! UserConnect(user.userID)

  override def postStop =
    // User Disconnected
    jobManager ! UserDisconnect(user.userID)

  def receive = LoggingReceive {

    case js: JsValue =>
      (js \ "type").validate[String].foreach {
        // User requests the job list for the widget
        case "GetJobList" =>
          jobManager ! GetJobList(user.userID)

        // connection test case
        case "Ping"       =>
          Logger.info("PING!")
        case _            =>
          Logger.error("Undefined Message: " + js.toString())
      }

    // Messages the user that the job widget needs to be updated
    case UpdateAllJobs =>
      out ! Json.obj("type" -> "UpdateAllJobs")

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
    case SendJobList(jobList : List[Job]) =>
      out ! Json.obj("type" -> "JobList",
                     "list" -> jobList.map(job =>
                        Json.obj("job_id"   -> job.jobID,
                                 "state"    -> job.status.no,
                                 "toolname" -> job.tool)))
  }
}
