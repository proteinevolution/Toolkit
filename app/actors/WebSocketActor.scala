package actors


import actors.JobManager._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import models.database.Job
import play.api.libs.json.{JsValue, Json}
import play.api.Logger
import reactivemongo.bson.BSONObjectID

/**
  * Actor that listens to the WebSocket and accepts messages from and passes messages to it.
  *
  */
object WebSocketActor {

  def props(sessionID : BSONObjectID, jobManager : ActorRef)(out: ActorRef) = Props(new WebSocketActor(sessionID, jobManager, out))
}

private final class WebSocketActor(sessionID : BSONObjectID, jobManager : ActorRef, out: ActorRef)  extends Actor with ActorLogging {


  override def preStart =
    // Connect to JobManager via Session ID
    jobManager ! UserConnect(sessionID)

  override def postStop =

    jobManager ! UserDisconnect(sessionID)

  def receive = LoggingReceive {

    case js: JsValue =>
      (js \ "type").validate[String].map {
        // User requests the job list for the widget
        case "GetJobList" =>
          Logger.info("Got a Request for the job list")
          jobManager ! GetJobList(sessionID, None)

        // connection test case
        case "Ping"       =>
          Logger.info("PING!")
        case _            =>
          Logger.error("Undefined Message: " + js.toString())
      }

    // Messages the user that the job widget needs to be updated
    case UpdateAllJobs =>
      Logger.info("Update All Jobs message sent,...")
      out ! Json.obj("type" -> "UpdateAllJobs")

    // Messages the user that there was a problem in handling the Job ID
    case JobIDUnknown =>
      Logger.info("User requested a unknown JobID")
      out ! Json.obj("type" -> "JobIDUnknown")

    // Messages the user about a change in the Job status
    case JobStateChanged(job, state) =>
      Logger.info("Job State Changed message sent,...")
      out ! Json.obj("type"     -> "UpdateJob",
                     "job_id"   -> job.jobID,
                     "state"    -> state.no,
                     "toolname" -> job.tool)

    // Sends the job list to the user
    case SendJobList(jobList : List[Job]) =>
      Logger.info("Job List message sent,...")
      out ! Json.obj("type" -> "JobList",
                     "list" -> jobList.map(job =>
                        Json.obj("job_id"   -> job.jobID,
                                 "state"    -> job.status.no,
                                 "toolname" -> job.tool)))

    //case UpdateJob(job : UserJob) =>
    // Sends the message that a job state has changed over the WebSocket. This is probably the most important
    // Real-time notification in this application
    //out ! Json.obj("type" -> "updatejob", "job_id" -> job.job_id, "state" -> job.getState.no, "toolname" -> job.tool.toolname)
  }
}
