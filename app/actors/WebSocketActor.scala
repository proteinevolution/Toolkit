package actors

import actors.ESManager.{AutoCompleteReply, AutoComplete}
import actors.JobManager._
import actors.UserManager._
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
  def props(userID : BSONObjectID, jobManager : ActorRef)(out: ActorRef) = {
    Props(new WebSocketActor(userID, jobManager, out))
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

        case "DeleteJob" =>
          (js \ "mainID").validate[String].asOpt match {
            case Some(mainIDString) =>
              BSONObjectID.parse(mainIDString).toOption match {
                case Some(mainID) =>
                  Logger.info(mainID.stringify)
                  userManager ! DeleteJob(userID, mainID)
                case None =>
                  Logger.info("BSON Parser Error" + js.toString())
              }
            case None =>
              Logger.info("JSON Parser Error " + js.toString())
          }

        case "AutoComplete" =>
          (js \ "queryString").validate[String].asOpt match {
            case Some(queryString) =>
              userManager ! AutoComplete(userID, queryString)
              Logger.info("a")
            case None =>
              Logger.info("JSON Parser Error " + js.toString())
          }


        case "ClearJob" =>
          (js \ "mainID").validate[String].asOpt match {
            case Some(mainIDString) =>
              BSONObjectID.parse(mainIDString).toOption match {
                case Some(mainID) =>
                  Logger.info(mainID.stringify)
                  userManager ! ClearJob(userID, mainID)
                case None =>
                  Logger.info("BSON Parser Error" + js.toString())
              }
            case None =>
              Logger.info("JSON Parser Error " + js.toString())
          }

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
      out ! Json.obj("type" -> "UpdateJob",
                     "job"  ->
                        Json.obj("mainID"   -> job.mainID.stringify,
                                 "job_id"   -> job.jobID,
                                 "state"    -> job.status,
                                 "toolname" -> job.tool))

    // Sends the job list to the user
    case SendJobList(userID : BSONObjectID, jobList : List[Job]) =>
      out ! Json.obj("type" -> "JobList",
                     "list" -> jobList.map(job =>
                        Json.obj("mainID"   -> job.mainID.stringify,
                                 "job_id"   -> job.jobID,
                                 "state"    -> job.status,
                                 "toolname" -> job.tool)))

    case AutoCompleteReply (userID : BSONObjectID, suggestionList : List[String]) =>
      out ! Json.obj("type" -> "JobList", "list" -> suggestionList)

  }
}
