package actors

import javax.inject.{Inject, Named}

import actors.JobActor.{JobStateChanged, StopWatch}
import actors.Master.{JobMessage, UserConnect}
import akka.actor.Actor
import akka.actor.ActorLogging
import akka.event.LoggingReceive
import akka.actor.ActorRef
import com.google.inject.assistedinject.Assisted
import models.database.User
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


class WebSocketActor @Inject() (@Named("master") master: ActorRef,
                                val reactiveMongoApi: ReactiveMongoApi,
                                @Assisted("userID") userID : BSONObjectID,
                                @Assisted("out") out: ActorRef)
  extends Actor with ActorLogging with CommonModule {

  override def preStart(): Unit = {
    master ! UserConnect(userID)
  }

  override def postStop(): Unit = {} // TODO Send UserDisconnect to Master


  def receive = LoggingReceive {

    case js: JsValue =>
      (js \ "type").validate[String].foreach {

        /**
          * Job interaction Requests
          */

        // Request to remove a Job from the user's view but it will remain stored
        case "ClearJob" =>

          (js \ "jobID").validate[String].asOpt match {

            case Some(jobID) =>
              Logger.info("Job: " + jobID + " is going to be cleared")
              modifyUser(BSONDocument(User.IDDB -> userID),
                BSONDocument("$pull" -> BSONDocument(User.JOBS -> jobID)))
              master ! JobMessage(jobID, StopWatch(self))
            case None => //
          }
      }


    case JobStateChanged(jobID, state) =>

      out ! Json.obj("type" -> "UpdateJob", "jobID" -> jobID, "state" -> state)
  }
}
