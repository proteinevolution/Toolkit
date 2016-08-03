package actors


import actors.JobManager.{JobStateChanged, UserConnect, UserDisconnect}
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
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


  /*
    case js: JsValue =>

      (js \ "type").validate[String].map {

        case "autocomplete" =>
            //(js \ "query").validate[String].map {
              //query => master ! AutoComplete(query)
            //}

        //case "getjoblist" => master ! GetJobList
        case "ping" => Logger.info("PING!")
      }
    */

    // Messages the user that there was a problem in handling the Job ID
    //case JobIDInvalid  => out ! Json.obj("type" -> "jobidinvalid")

    case JobStateChanged(job, state) =>

      out ! Json.obj("type" -> "updatejob", "job_id" -> job.jobID, "state" -> state.no, "toolname" -> "foobar")


    /*
     * Messages the user about a change in the Job status
     */
    //case UpdateJob(job : UserJob) =>
      // Sends the message that a job state has changed over the WebSocket. This is probably the most important
      // Real-time notification in this application
      //out ! Json.obj("type" -> "updatejob", "job_id" -> job.job_id, "state" -> job.getState.no, "toolname" -> job.tool.toolname)

    //case SendJobList(jobSeq : Seq[UserJob]) =>
    //  out ! Json.obj("type" -> "joblist", "list" -> createJobObjList(jobSeq))

    //case AutoCompleteSend (jobSeq : Seq[DBJob]) =>
    //  out ! Json.obj("type" -> "autocomplete", "list" -> createJobObjListDB(jobSeq))
  }
}
