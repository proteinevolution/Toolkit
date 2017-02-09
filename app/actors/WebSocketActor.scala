package actors

import javax.inject.Inject

import actors.JobActor._
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import com.google.inject.assistedinject.Assisted
import models.database.{Job, User}
import models.job.JobActorAccess
import modules.CommonModule
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

/**
  * Actor that listens to the WebSocket and accepts messages from and passes messages to it.
  *
  */
object WebSocketActor {
  trait Factory {

    def apply(@Assisted("userID") userID: BSONObjectID, @Assisted("out") out: ActorRef): Actor
  }
}

class WebSocketActor @Inject() (val reactiveMongoApi: ReactiveMongoApi,
                                jobActorAccess: JobActorAccess,
                                @Assisted("userID") userID : BSONObjectID,
                                @Assisted("out") out: ActorRef)
  extends Actor with ActorLogging with CommonModule {

  override def preStart(): Unit = {
    jobActorAccess.identifyUser(RegisterUser(userID, self))
  } // Can not send any messages at this point of init
  override def postStop(): Unit = {
    jobActorAccess.identifyUser(UnregisterUser(userID, self))
  } // TODO Send UserDisconnect to Master


  def receive = LoggingReceive {

    case js: JsValue =>
      (js \ "type").validate[String].foreach {

        // Message containing a List of Jobs the WebSocket wants to register to
        case "RegisterJobs" =>

          (js \ "jobIDs").validate[Seq[String]].asOpt match {

            case Some(jobIDs) => jobIDs.foreach { jobID =>
              jobActorAccess.sendToJobActor(jobID, StartWatch(jobID, userID))
            }
            case None => // Client has send strange message over the Websocket
          }


        // Request to remove a Job from the user's view but it will remain stored
        case "ClearJob" =>

          (js \ "jobID").validate[String].asOpt match {
            case Some(jobID) =>
              jobActorAccess.sendToJobActor(jobID, StopWatch(jobID, userID))
            case None => //
          }
      }

    //case JobStateChanged(jobID, state) =>

      //out ! Json.obj("type" -> "UpdateJob", "jobID" -> jobID, "state" -> state)

    case PushJob(job : Job) =>
      Logger.info("WS Log: " + job.jobID + " is now " + job.status.toString)
      out ! Json.obj("type" -> "UpdateJob", "job" -> job.cleaned())

    case ClearJob(jobID : String) =>
      out ! Json.obj("type" -> "ClearJob", "jobID" -> jobID)
  }
}
