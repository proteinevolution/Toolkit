package actors


import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import akka.actor.Props
import models.database.DBJob
import models.distributed.FrontendMasterProtocol
import models.jobs.UserJob
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.Logger
import models.jobs.UserJob.JobStateChanged

/**
  * Actor that listens to the WebSocket and accepts messages from and passes messages to it.
  *
  */
object WebSocketActor {


  def props(sessionID : String, master : ActorRef)(out: ActorRef) = Props(new WebSocketActor(sessionID, master, out))
}

class WebSocketActor(sessionID : String, master : ActorRef, out: ActorRef)  extends Actor with ActorLogging {


  override def preStart = {

    Logger.info("WebSocket for " + sessionID + " has started")
    master ! FrontendMasterProtocol.Subscribe(sessionID)
  }

  /**
    * Returns a Sequence of DBJob as a JSON Array
    *
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
    *
    * @param jobSeq Sequence of UserJobs
    * @return
    */
  def createJobObjList (jobSeq : Seq[UserJob]) : JsArray = {
    JsArray(for (job <- jobSeq) yield {
      Json.obj("t" -> job.tool.toolname,
               "s" -> job.getState.no,
               "i" -> job.jobID)
    })
  }

  def receive = LoggingReceive {


    case js: JsValue =>

      (js \ "type").validate[String].map {

        case "autocomplete" =>
            //(js \ "query").validate[String].map {
              //query => master ! AutoComplete(query)
            //}

        //case "getjoblist" => master ! GetJobList
        case "ping" => Logger.info("PING!")
      }

    // Messages the user that there was a problem in handling the Job ID
    //case JobIDInvalid  => out ! Json.obj("type" -> "jobidinvalid")

    case JobStateChanged(jobID, state, toolname) =>

      out ! Json.obj("type" -> "updatejob", "job_id" -> jobID, "state" -> state.no, "toolname" -> toolname)


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
