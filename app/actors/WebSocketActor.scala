package actors

import javax.inject.{ Inject, Named }

import actors.ClusterMonitor._
import actors.JobActor._
import actors.WebSocketActor.{ ChangeSessionID, LogOut, MaintenanceAlert }
import akka.actor.{ Actor, ActorLogging, ActorRef, PoisonPill }
import akka.event.LoggingReceive
import com.google.inject.assistedinject.Assisted
import controllers.UserSessions
import models.database.jobs.Job
import models.job.JobActorAccess
import modules.LocationProvider
import play.api.Logger
import play.api.cache._
import play.api.libs.json.{ JsValue, Json }
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Actor that listens to the WebSocket and accepts messages from and passes messages to it.
  *
  */
object WebSocketActor {
  case class ChangeSessionID(sessionID: BSONObjectID)
  case object LogOut
  case object MaintenanceAlert

  trait Factory {
    def apply(@Assisted("sessionID") sessionID: BSONObjectID, @Assisted("out") out: ActorRef): Actor
  }
}

final class WebSocketActor @Inject()(val locationProvider: LocationProvider,
                                     @Named("clusterMonitor") clusterMonitor: ActorRef,
                                     @Assisted("out") out: ActorRef,
                                     jobActorAccess: JobActorAccess,
                                     userSessions: UserSessions,
                                     @NamedCache("userCache") val userCache: CacheApi,
                                     @NamedCache("wsActorCache") val wsActorCache: CacheApi,
                                     @Assisted("sessionID") private var sessionID: BSONObjectID)
    extends Actor
    with ActorLogging {

  override def preStart(): Unit = {
    // Grab the user from cache to ensure a working job
    clusterMonitor ! Connect(self)
    userSessions.getUser(sessionID).foreach {
      case Some(user) =>
        wsActorCache.get(user.userID.stringify) match {
          case Some(wsActors) =>
            val actorSet = (wsActors: List[ActorRef]).::(self)
            wsActorCache.set(user.userID.stringify, actorSet)
          case None =>
            wsActorCache.set(user.userID.stringify, List(self))
        }
      case None =>
        self ! PoisonPill
    }
  } // TODO May not be able to send any messages at this point of init

  override def postStop(): Unit = {
    clusterMonitor ! Disconnect(self)
    /*getUser(sessionID).foreach {
      case Some(user) =>
        wsActorCache.get(user.userID.stringify) match {
          case Some(wsActors) =>
            val actorSet: List[ActorRef] = wsActors: List[ActorRef]
            val newActorSet              = actorSet.filter(_ == self)
            wsActorCache.set(user.userID.stringify, newActorSet)
          case None =>
        }
      case None =>
        self ! PoisonPill // PoisonPill here is pretty useless since postStop means that the actor is shutting down
    } */

    /**
      *
      *  do we need to have persistent actors? if so,
      *  let's use akka-persistence instead of the cache (which would not work).
      *  actors must be removed from
      *  the cache at some point anyway.
      */
    userSessions.getUser(sessionID).foreach {
      case Some(user) =>
        wsActorCache.remove(user.userID.stringify)
    }
  }

  def receive = LoggingReceive {

    case js: JsValue =>
      userSessions.getUser(sessionID).foreach {
        case Some(user) =>
          (js \ "type").validate[String].foreach {

            // Message containing a List of Jobs the user wants to register for the job list
            case "RegisterJobs" =>
              (js \ "jobIDs").validate[Seq[String]].asOpt match {

                case Some(jobIDs) =>
                  jobIDs.foreach { jobID =>
                    jobActorAccess.sendToJobActor(jobID, AddToWatchlist(jobID, user.userID))
                  }
                case None => // Client has send strange message over the Websocket
              }

            // Request to remove a Job from the user's Joblist
            case "ClearJob" =>
              (js \ "jobIDs").validate[Seq[String]].asOpt match {
                case Some(jobIDs) =>
                  jobIDs.foreach { jobID =>
                    jobActorAccess.sendToJobActor(jobID, RemoveFromWatchlist(jobID, user.userID))
                  }
                case None => //
              }

            // Request to receive load messages
            case "RegisterLoad" =>
              Logger.info("Received RegisterLoad message.")
              clusterMonitor ! Connect(self)

            //// Request to no longer receive load messages
            case "UnregisterLoad" =>
              clusterMonitor ! Disconnect(self)
          }
        case None =>
          self ! PoisonPill
      }

    case PushJob(job: Job) =>
      out ! Json.obj("type" -> "PushJob", "job" -> job.cleaned())

    case UpdateLog(jobID: String) =>
      out ! Json.obj("type" -> "UpdateLog", "jobID" -> jobID)

    case UpdateLoad(load: Double) =>
      out ! Json.obj("type" -> "UpdateLoad", "load" -> load)

    case ClearJob(jobID: String, deleted: Boolean) =>
      out ! Json.obj("type" -> "ClearJob", "jobID" -> jobID, "deleted" -> deleted)

    case ChangeSessionID(sessionID: BSONObjectID) =>
      this.sessionID = sessionID

    case LogOut =>
      out ! Json.obj("type" -> "LogOut")

    case MaintenanceAlert =>
      out ! Json.obj("type" -> "MaintenanceAlert")
  }
}
