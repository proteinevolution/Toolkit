/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.message.actors

import java.nio.file.{ Files, Paths }
import java.time.ZonedDateTime

import akka.actor.{ Actor, ActorLogging, ActorRef, PoisonPill }
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.assistedinject.Assisted
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.auth.models.Session.ChangeSessionID
import de.proteinevolution.cluster.ClusterSubscriber.UpdateLoad
import de.proteinevolution.cluster.api.SGELoad
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.jobs.JobState.Running
import de.proteinevolution.jobs.actors.JobActor._
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.jobs.services.JobActorAccess
import de.proteinevolution.message.actors.WebSocketActor.{ LogOut, MaintenanceAlert }
import de.proteinevolution.tools.ToolConfig
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Named }
import play.api.Configuration
import play.api.cache.{ NamedCache, SyncCacheApi }
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

final class WebSocketActor @Inject()(
    @Assisted("out") out: ActorRef,
    jobActorAccess: JobActorAccess,
    userSessions: UserSessions,
    constants: ConstantsV2,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
    @Assisted("sessionID") sessionID: BSONObjectID,
    toolConfig: ToolConfig,
    @Named("clusterSubscriber") clusterSubscriber: ActorRef
)(implicit ec: ExecutionContext, config: Configuration)
    extends Actor
    with ActorLogging {

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[UpdateLoad])
    clusterSubscriber ? SGELoad.Ask
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
  }

  override def postStop(): Unit = {
    userSessions
      .getUser(sessionID)
      .map(_.foreach { user =>
        wsActorCache.get[List[ActorRef]](user.userID.stringify).foreach { wsActors =>
          val actorSet: List[ActorRef] = wsActors: List[ActorRef]
          val newActorSet              = actorSet.filterNot(_ == self)
          wsActorCache.set(user.userID.stringify, newActorSet)
        }
      })
    context.system.eventStream.unsubscribe(self, classOf[UpdateLoad])
    log.info(s"[WSActor] Websocket closed for session ${sessionID.stringify}")
  }

  private def active(sid: BSONObjectID): Receive = {

    case json: Json =>
      userSessions.getUser(sid).foreach {
        case Some(user) =>
          json.hcursor.get[String]("type").toOption.foreach {

            // Message containing a List of Jobs the user wants to register for the job list
            case "RegisterJobs" =>
              json.hcursor.get[List[String]]("jobIDs").map { jobIDs =>
                jobIDs.foreach { jobID =>
                  jobActorAccess.sendToJobActor(jobID, AddToWatchlist(jobID, user.userID))
                }
              }

            // Request to remove a Job from the user's Joblist
            case "ClearJob" =>
              json.hcursor.get[List[String]]("jobIDs").map { jobIDs =>
                jobIDs.foreach { jobID =>
                  jobActorAccess.sendToJobActor(jobID, RemoveFromWatchlist(jobID, user.userID))
                }
              }

            // Received a ping, so we return a pong
            case "Ping" =>
              json.hcursor.get[Long]("date").map { msTime =>
                //log.info(s"[WSActor] Ping from session ${sid.stringify} with msTime $msTime")
                out ! JsonObject("type" -> Json.fromString("Pong"), "date" -> Json.fromLong(msTime)).asJson
              }

            // Received a pong message from the client - lets see how long it took
            case "Pong" =>
              json.hcursor.get[Long]("date").map { msTime =>
                val ping = ZonedDateTime.now.toInstant.toEpochMilli - msTime
                log.info(s"[WSActor] Ping of session ${sid.stringify} is ${ping}ms.")
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
