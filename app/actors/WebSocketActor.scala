package actors

import javax.inject.{ Inject, Named }

import actors.ClusterMonitor._
import actors.JobActor._
import actors.WebSocketActor.{ ChangeSessionID, LogOut, MaintenanceAlert }
import akka.actor.{ Actor, ActorLogging, ActorRef, PoisonPill }
import akka.event.LoggingReceive
import com.google.inject.assistedinject.Assisted
import models.UserSessions
import de.proteinevolution.models.database.jobs.{ Job, Running }
import play.api.Logger
import play.api.cache._
import play.api.libs.json.{ JsValue, Json }
import reactivemongo.bson.BSONObjectID
import java.nio.file.{ Files, Paths }
import java.time.ZonedDateTime

import de.proteinevolution.common.LocationProvider
import de.proteinevolution.models.Constants
import services.JobActorAccess

import scala.concurrent.ExecutionContext

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

final class WebSocketActor @Inject()(
    val locationProvider: LocationProvider,
    @Named("clusterMonitor") clusterMonitor: ActorRef,
    @Assisted("out") out: ActorRef,
    jobActorAccess: JobActorAccess,
    userSessions: UserSessions,
    constants: Constants,
    @NamedCache("userCache") val userCache: SyncCacheApi,
    @NamedCache("wsActorCache") val wsActorCache: SyncCacheApi,
    @Assisted("sessionID") private var sessionID: BSONObjectID
)(implicit ec: ExecutionContext)
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
    userSessions.getUser(sessionID).foreach {
      case Some(user) =>
        wsActorCache.get(user.userID.stringify) match {
          case Some(wsActors) =>
            val actorSet: List[ActorRef] = wsActors: List[ActorRef]
            val newActorSet              = actorSet.filterNot(_ == self)
            wsActorCache.set(user.userID.stringify, newActorSet)
          case None => ()
        }
      case None => ()
    }

    Logger.info(s"[WSActor] Websocket closed for session ${sessionID.stringify}")
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
                case None => // Client has sent strange message over the Websocket
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

            // Received a ping, so we return a pong
            case "Ping" =>
              (js \ "date").validate[Long].asOpt match {
                case Some(msTime) =>
                  //Logger.info(s"[WSActor] Ping from session ${sessionID.stringify} with msTime $msTime")
                  out ! Json.obj("type" -> "Pong", "date" -> msTime)
                case None =>
              }

            // Received a pong message from the client - lets see how long it took
            case "Pong" =>
              (js \ "date").validate[Long].asOpt match {
                case Some(msTime) =>
                  val ping = ZonedDateTime.now.toInstant.toEpochMilli - msTime
                  Logger.info(s"[WSActor] Ping of session ${sessionID.stringify} is ${ping}ms.")
                case None =>
              }
          }
        case None =>
          self ! PoisonPill
      }

    case PushJob(job: Job) =>
      out ! Json.obj("type" -> "PushJob", "job" -> job.cleaned())

    case UpdateLog(jobID: String) =>
      out ! Json.obj("type" -> "UpdateLog", "jobID" -> jobID)

    case WatchLogFile(job: Job) =>
      // Do filewatching here
      val file = s"${constants.jobPath}${job.jobID}${constants.SEPARATOR}results${constants.SEPARATOR}process.log"
      //Logger.info("Watching: " + file)
      if (job.status.equals(Running)) {
        if (Files.exists(Paths.get(file))) {
          val source = scala.io.Source.fromFile(file)
          val lines = try source.mkString
          finally source.close()
          //println(lines)
          out ! Json.obj("type" -> "WatchLogFile", "jobID" -> job.jobID, "lines" -> lines)
        }
      }

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
