package actors

import actors.ESManager.{SearchReply, Search, AutoCompleteReply, AutoComplete}
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

        /**
          * Job interaction Requests
          */
        // User requests the job list for the widget
        case "GetJobList" =>
          userManager ! GetJobList(userID)

        // Request to delete a job completely
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

        // Request to add an existing job to the users view
        case "AddJob" =>
          (js \ "mainID").validate[String].asOpt match {
            case Some(mainIDString) =>
              BSONObjectID.parse(mainIDString).toOption match {
                case Some(mainID) =>
                  Logger.info(mainID.stringify)
                  userManager ! AddJob(userID, mainID)
                case None =>
                  Logger.info("BSON Parser Error" + js.toString())
              }
            case None =>
              Logger.info("JSON Parser Error " + js.toString())
          }

        // Request to remove a Job from the user's view but it will remain stored
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

        // Request to start a Job which has been put on hold/ which has been Prepared
        case "StartJob" =>
          (js \ "mainID").validate[String].asOpt match {
            case Some(mainIDString) =>
              BSONObjectID.parse(mainIDString).toOption match {
                case Some(mainID) =>
                  Logger.info(mainID.stringify)
                  userManager ! StartJob(userID, mainID)
                case None =>
                  Logger.info("BSON Parser Error" + js.toString())
              }
            case None =>
              Logger.info("JSON Parser Error " + js.toString())
          }

        /**
          * Search oriented requests
          */
        // Request to get an auto complete
        case "AutoComplete" =>
          (js \ "queryString").validate[String].asOpt match {
            case Some(queryString) =>
              // To keep this small we might use an int as identifier from which search element the request came
              val element = (js \ "element").validate[Int].asOpt.getOrElse(0)
              userManager ! AutoComplete(userID, queryString, element)
              Logger.info("a")
            case None =>
              Logger.info("JSON Parser Error " + js.toString())
          }

        // User wants to search the database
        case "Search" =>
          (js \ "queryString").validate[String].asOpt match {
            case Some(queryString) =>
              // To keep this small we might use an int as identifier from which search element the request came
              val element = (js \ "element").validate[Int].asOpt.getOrElse(0)
              Logger.info("WSactor: Find " + queryString)
              userManager ! Search(userID, queryString, element)
            case None =>
              Logger.info("JSON Parser Error " + js.toString())
        }

        /**
          * Connection oriented requests
          */
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

    // Sends a list of possible strings for the auto complete
    case AutoCompleteReply (userID : BSONObjectID, suggestionList : List[String], element) =>
      println("AutoSuggestion: " + suggestionList)
      out ! Json.obj("type" -> "AutoCompleteReply", "list" -> suggestionList, "element" -> element)

    // Sends a list of jobs for the search
    case SearchReply(userID : BSONObjectID, jobList : List[Job], element) =>
      out ! Json.obj("type" -> "SearchReply",
                     "list" -> jobList.map(job =>
                        Json.obj("mainID"   -> job.mainID.stringify,
                                "job_id"   -> job.jobID,
                                "state"    -> job.status,
                                "toolname" -> job.tool)),
                     "element" -> element)
  }
}
