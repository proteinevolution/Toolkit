package actors

import javax.inject.Inject

import actors.JobActor._
import akka.actor.{PoisonPill, Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import com.google.inject.assistedinject.Assisted
import controllers.UserSessions
import models.database.jobs.Job
import models.database.users.User
import models.job.JobActorAccess
import modules.{LocationProvider, CommonModule}
import play.api.Logger
import play.api.cache._
import play.api.libs.json.{JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Actor that listens to the WebSocket and accepts messages from and passes messages to it.
  *
  */
object WebSocketActor {
  trait Factory {

    def apply(@Assisted("sessionID") sessionID: BSONObjectID, @Assisted("out") out: ActorRef): Actor
  }
}

class WebSocketActor @Inject() (  val reactiveMongoApi: ReactiveMongoApi,
                         implicit val locationProvider: LocationProvider,
                                      jobActorAccess  : JobActorAccess,
@NamedCache("userCache") implicit val userCache       : CacheApi,
               @Assisted("sessionID") sessionID       : BSONObjectID,
                     @Assisted("out") out             : ActorRef)
                              extends Actor with ActorLogging with CommonModule with UserSessions {

  override def preStart(): Unit = {
    getUser(sessionID).foreach {
      case Some(user) =>
        jobActorAccess.identifyUser(RegisterUser(user.userID, self))
      case None =>
        self ! PoisonPill
    }
  } // May not be able to send any messages at this point of init
  override def postStop(): Unit = {
    getUser(sessionID).foreach {
      case Some(user) =>
        jobActorAccess.identifyUser(UnregisterUser(user.userID, self))
      case None =>
        self ! PoisonPill
    }
  }


  def receive = LoggingReceive {

    case js: JsValue =>
      getUser(sessionID).foreach {
        case Some(user) =>
        (js \ "type").validate[String].foreach {

          // Message containing a List of Jobs the WebSocket wants to register to
          case "RegisterJobs" =>

            (js \ "jobIDs").validate[Seq[String]].asOpt match {

              case Some(jobIDs) => jobIDs.foreach { jobID =>
                jobActorAccess.sendToJobActor(jobID, StartWatch(jobID, user.userID))
              }
              case None => // Client has send strange message over the Websocket
            }


          // Request to remove a Job from the user's view but it will remain stored
          case "ClearJob" =>

            (js \ "jobID").validate[String].asOpt match {
              case Some(jobID) =>
                jobActorAccess.sendToJobActor(jobID, StopWatch(jobID, user.userID))
              case None => //
            }
        }
        case None =>
          self ! PoisonPill
      }

    case PushJob(job : Job) =>
      //Logger.info("WS Log: " + job.jobID + " is now " + job.status.toString)
      out ! Json.obj("type" -> "UpdateJob", "job" -> job.cleaned())

    case ClearJob(jobID : String) =>
      //Logger.info("WS Log: " + jobID + " clear message sent")
      out ! Json.obj("type" -> "ClearJob", "jobID" -> jobID)
  }
}
