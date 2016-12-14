package actors

import actors.JobActor.JobStateChanged
import actors.Master.UserConnect
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
  def props(userID : BSONObjectID, master: ActorRef)(out: ActorRef) = {
    Props(new WebSocketActor(userID, master, out))
  }
}

private final class WebSocketActor(userID : BSONObjectID, master: ActorRef, out: ActorRef)  extends Actor with ActorLogging {

  override def preStart =
    // Connect to JobManager via Session ID
    master ! UserConnect(userID)

  override def postStop = {} // TODO Send UserDisconnect to Master



  def receive = LoggingReceive {

    case js: JsValue =>
      (js \ "type").validate[String].foreach {

        /**
          * Job interaction Requests
          */
        // Request to delete a job completely
        case "DeleteJob" =>
          (js \ "mainID").validate[String].asOpt match {
            case Some(mainIDString) =>
              BSONObjectID.parse(mainIDString).toOption match {
                case Some(mainID) =>
                  Logger.info(mainID.stringify)
                  //userManager ! ForceDeleteJob(userID, mainID)
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
                  //userManager ! AddJob(userID, mainID)
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
                  //userManager ! ClearJob(userID, mainID)
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
                  //userManager ! StartJob(userID, mainID)
                case None =>
                  Logger.info("BSON Parser Error" + js.toString())
              }
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

    /*
    case RunningJobMessage(mainID, message) =>
      out ! Json.obj("type" -> "jobMessage",
                     "mainID" -> mainID.stringify,
                     "message" -> message)
    */

    // Messages the user that there was a problem in handling the Job ID
    //case JobIDUnknown =>
    //  out ! Json.obj("type" -> "JobIDUnknown")

    case JobStateChanged(jobID, state) =>

      out ! Json.obj("type" -> "UpdateJob", "jobID" -> jobID, "state" -> state)
  }
}
