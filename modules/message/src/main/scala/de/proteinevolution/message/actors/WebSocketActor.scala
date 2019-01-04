package de.proteinevolution.message.actors

import java.nio.file.{ Files, Paths }
import java.time.ZonedDateTime

import akka.actor.{ Actor, ActorLogging, ActorRef, PoisonPill }
import akka.event.LoggingReceive
import com.google.inject.assistedinject.Assisted
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.cluster.actors.ClusterMonitor.{ Connect, Disconnect, UpdateLoad }
import de.proteinevolution.jobs.actors.JobActor._
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.jobs.services.JobActorAccess
import de.proteinevolution.message.actors.WebSocketActor.{ LogOut, MaintenanceAlert }
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.JobState.Running
import de.proteinevolution.auth.models.Session.ChangeSessionID
import de.proteinevolution.tools.ToolConfig
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Named }
import play.api.Configuration
import play.api.cache.{ NamedCache, SyncCacheApi }
import reactivemongo.bson.BSONObjectID

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

    case json: Json =>
      userSessions.getUser(sid).foreach {
        case Some(user) =>
          json.hcursor.get[String]("type").toOption.foreach {

            // Message containing a List of Jobs the user wants to register for the job list
            case "RegisterJobs" =>
              json.hcursor.get[List[String]]("jobIDs") match {
                case Right(jobIDs) =>
                  jobIDs.foreach { jobID =>
                    jobActorAccess.sendToJobActor(jobID, AddToWatchlist(jobID, user.userID))
                  }
                case Left(_) => // Client has sent strange message over the Websocket
              }

            // Request to remove a Job from the user's Joblist
            case "ClearJob" =>
              json.hcursor.get[List[String]]("jobIDs") match {
                case Right(jobIDs) =>
                  jobIDs.foreach { jobID =>
                    jobActorAccess.sendToJobActor(jobID, RemoveFromWatchlist(jobID, user.userID))
                  }
                case Left(_) => //
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
              json.hcursor.get[Long]("date") match {
                case Right(msTime) =>
                  //log.info(s"[WSActor] Ping from session ${sid.stringify} with msTime $msTime")
                  out ! JsonObject("type" -> Json.fromString("Pong"), "date" -> Json.fromLong(msTime)).asJson
                case Left(_) =>
              }

            // Received a pong message from the client - lets see how long it took
            case "Pong" =>
              json.hcursor.get[Long]("date") match {
                case Right(msTime) =>
                  val ping = ZonedDateTime.now.toInstant.toEpochMilli - msTime
                  log.info(s"[WSActor] Ping of session ${sid.stringify} is ${ping}ms.")
                case Left(_) =>
              }
          }
        case None =>
          self ! PoisonPill
      }

    case PushJob(job: Job) =>
      out ! JsonObject("type" -> Json.fromString("PushJob"), "job" -> job.cleaned(toolConfig).asJson).asJson

    case ShowNotification(notificationType: String, tag: String, title: String, body: String) =>
      out ! JsonObject(
        "type"             -> Json.fromString("ShowNotification"),
        "tag"              -> Json.fromString(tag),
        "title"            -> Json.fromString(title),
        "body"             -> Json.fromString(body),
        "notificationType" -> Json.fromString(notificationType)
      ).asJson

    case UpdateLog(jobID: String) =>
      out ! JsonObject(
        "type"  -> Json.fromString("UpdateLog"),
        "jobID" -> Json.fromString(jobID)
      ).asJson

    case WatchLogFile(job: Job) =>
      // Do filewatching here
      val file = s"${constants.jobPath}${job.jobID}${constants.SEPARATOR}results${constants.SEPARATOR}process.log"
      if (job.status.equals(Running)) {
        if (Files.exists(Paths.get(file))) {
          val source = scala.io.Source.fromFile(file)
          val lines  = source.mkString
          // val lines = File(file).lineIterator.mkString // use buffered source since it behaves differently
          out ! JsonObject(
            "type"  -> Json.fromString("WatchLogFile"),
            "jobID" -> Json.fromString(job.jobID),
            "lines" -> Json.fromString(lines)
          ).asJson
          source.close()
        }
      }

    case UpdateLoad(load: Double) =>
      out ! JsonObject(
        "type" -> Json.fromString("UpdateLoad"),
        "load" -> Json.fromDoubleOrNull(load)
      ).asJson

    case ClearJob(jobID: String, deleted: Boolean) =>
      out ! JsonObject(
        "type"    -> Json.fromString("ClearJob"),
        "jobID"   -> Json.fromString(jobID),
        "deleted" -> Json.fromBoolean(deleted)
      ).asJson

    case ChangeSessionID(newSid: BSONObjectID) =>
      context.become(active(newSid))

    case LogOut =>
      out ! JsonObject("type" -> Json.fromString("LogOut")).asJson

    case MaintenanceAlert =>
      out ! JsonObject("type" -> Json.fromString("MaintenanceAlert")).asJson
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
