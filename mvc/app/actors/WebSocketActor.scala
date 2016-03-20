package actors


import actors.UserActor._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import models.database.DBJob
import models.jobs.{JobState, UserJob}
import play.api.libs.json.{JsValue, JsArray, Json, JsObject}
import play.api.Logger

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

  /**
    * Returns a Sequence of DBJob as a JSON Array
    * @param dbJobSeq Sequence of DBJob
    * @return
    */
  def createJobObjListDB (dbJobSeq : Seq[DBJob]) : JsArray = {
    JsArray(for (dbJob <- dbJobSeq) yield {
      Json.obj("t" -> dbJob.toolname,
               "s" -> dbJob.job_state.no,
               "i" -> dbJob.job_id)
    })
  }

  /**
    * Returns a Sequence of user Jobs as a JSON Array
    * @param jobSeq Sequence of UserJobs
    * @return
    */
  def createJobObjList (jobSeq : Seq[UserJob]) : JsArray = {
    JsArray(for (job <- jobSeq) yield {
      Json.obj("t" -> job.toolname,
               "s" -> job.getState.no,
               "i" -> job.job_id)
    })
  }

  def receive = LoggingReceive {


    case js: JsValue =>

      (js \ "type").validate[String].map {

        case "autocomplete" =>
            (js \ "query").validate[String].map {
              query => userActor ! AutoComplete(query)
            }

        case "getjoblist" =>
            userActor ! GetJobList
        case "ping" => Logger.info("PING!")
      }

    // Messages the user that there was a problem in handling the Job ID
    case JobIDInvalid  => out ! Json.obj("type" -> "jobidinvalid")

    /*
     * Messages the user about a change in the Job status
     */
    case UpdateJob(job : UserJob) =>
      // Sends the message that a job state has changed over the WebSocket. This is probably the most important
      // Real-time notification in this application
      out ! Json.obj("type" -> "updatejob", "job_id" -> job.job_id, "state" -> job.getState.no, "toolname" -> job.toolname)

    case SendJobList(jobSeq : Seq[UserJob]) =>
      out ! Json.obj("type" -> "joblist", "list" -> createJobObjList(jobSeq))

    case AutoCompleteSend (jobSeq : Seq[DBJob]) =>
      out ! Json.obj("type" -> "autocomplete", "list" -> createJobObjListDB(jobSeq))
  }
}
