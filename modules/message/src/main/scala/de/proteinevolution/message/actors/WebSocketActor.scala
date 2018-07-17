package de.proteinevolution.message.actors

import java.nio.file.{ Files, Paths }
import java.time.ZonedDateTime

import akka.actor.{ Actor, ActorLogging, ActorRef, PoisonPill }
import akka.event.LoggingReceive
import com.google.inject.assistedinject.Assisted
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.cluster.actors.ClusterMonitor.{ Connect, Disconnect, UpdateLoad }
import de.proteinevolution.jobs.actors.JobActor._
import de.proteinevolution.jobs.services.JobActorAccess
import de.proteinevolution.message.actors.WebSocketActor.{ LogOut, MaintenanceAlert }
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.models.database.jobs.JobState.Running
import de.proteinevolution.models.message.Session.ChangeSessionID
import de.proteinevolution.services.ToolConfig
import javax.inject.{ Inject, Named }
import play.api.Configuration
import play.api.cache.{ NamedCache, SyncCacheApi }
import play.api.libs.json.{ JsValue, Json }
import reactivemongo.bson.BSONObjectID
// import better.files._
import scala.concurrent.ExecutionContext

final class WebSocketActor @Inject()(
    @Named("clusterMonitor") clusterMonitor: ActorRef,
    @Assisted("out") out: ActorRef,
    jobActorAccess: JobActorAccess,
    userSessions: UserSessions,
    constants: ConstantsV2,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
    @Assisted("sessionID") sessionID: BSONObjectID,
    toolConfig: ToolConfig,
    implicit val config: Configuration
)(implicit ec: ExecutionContext)
    extends Actor
    with ActorLogging {

  override def preStart(): Unit = {
    // Grab the user from cache to ensure a working job
    clusterMonitor ! Connect(self)
    userSessions.getUser(sessionID).foreach {
      case Some(user) =>
        wsActorCache.get[List[ActorRef]](user.userID.stringify) match {
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
        wsActorCache.get[List[ActorRef]](user.userID.stringify) match {
          case Some(wsActors) =>
            val actorSet: List[ActorRef] = wsActors: List[ActorRef]
            val newActorSet              = actorSet.filterNot(_ == self)
            wsActorCache.set(user.userID.stringify, newActorSet)
          case None => ()
        }
      case None => ()
    }

    log.info(s"[WSActor] Websocket closed for session ${sessionID.stringify}")
  }

  private def active(sid: BSONObjectID): Receive = {

    case js: JsValue =>
      userSessions.getUser(sid).foreach {
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
              log.info("Received RegisterLoad message.")
              clusterMonitor ! Connect(self)

            //// Request to no longer receive load messages
            case "UnregisterLoad" =>
              clusterMonitor ! Disconnect(self)

            // Received a ping, so we return a pong
            case "Ping" =>
              (js \ "date").validate[Long].asOpt match {
                case Some(msTime) =>
                  //log.info(s"[WSActor] Ping from session ${sid.stringify} with msTime $msTime")
                  out ! Json.obj("type" -> "Pong", "date" -> msTime)
                case None =>
              }

            // Received a pong message from the client - lets see how long it took
            case "Pong" =>
              (js \ "date").validate[Long].asOpt match {
                case Some(msTime) =>
                  val ping = ZonedDateTime.now.toInstant.toEpochMilli - msTime
                  log.info(s"[WSActor] Ping of session ${sid.stringify} is ${ping}ms.")
                case None =>
              }
          }
        case None =>
          self ! PoisonPill
      }

    case PushJob(job: Job) =>
      out ! Json.obj("type" -> "PushJob", "job" -> job.cleaned(toolConfig))

    case ShowNotification(notificationType: String, tag: String, title: String, body: String) =>
      out ! Json.obj("type"             -> "ShowNotification",
                     "tag"              -> tag,
                     "title"            -> title,
                     "body"             -> body,
                     "notificationType" -> notificationType)

    case UpdateLog(jobID: String) =>
      out ! Json.obj("type" -> "UpdateLog", "jobID" -> jobID)

    case WatchLogFile(job: Job) =>
      // Do filewatching here
      val file = s"${constants.jobPath}${job.jobID}${constants.SEPARATOR}results${constants.SEPARATOR}process.log"
      if (job.status.equals(Running)) {
        if (Files.exists(Paths.get(file))) {
          val source = scala.io.Source.fromFile(file)
          val lines  = source.mkString
          // val lines = File(file).lineIterator.mkString // use buffered source since it behaves differently
          out ! Json.obj("type" -> "WatchLogFile", "jobID" -> job.jobID, "lines" -> lines)
          source.close()
        }
      }

    case UpdateLoad(load: Double) =>
      out ! Json.obj("type" -> "UpdateLoad", "load" -> load)

    case ClearJob(jobID: String, deleted: Boolean) =>
      out ! Json.obj("type" -> "ClearJob", "jobID" -> jobID, "deleted" -> deleted)

    case ChangeSessionID(newSid: BSONObjectID) =>
      context.become(active(newSid))

    case LogOut =>
      out ! Json.obj("type" -> "LogOut")

    case MaintenanceAlert =>
      out ! Json.obj("type" -> "MaintenanceAlert")
  }

  override def receive = LoggingReceive {
    active(sessionID)
  }
}

object WebSocketActor {

  case object LogOut
  case object MaintenanceAlert

  trait Factory {
    def apply(@Assisted("sessionID") sessionID: BSONObjectID, @Assisted("out") out: ActorRef): Actor
  }

}
