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
import de.proteinevolution.auth.models.Session.{ ChangeSessionID, LogOut }
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.cluster.ClusterSubscriber.UpdateLoad
import de.proteinevolution.cluster.api.SGELoad
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.database.jobs.JobState.Running
import de.proteinevolution.jobs.actors.JobActor._
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.jobs.services.JobActorAccess
import de.proteinevolution.message.actors.WebSocketActor.MaintenanceAlert
import de.proteinevolution.tools.ToolConfig
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Named }
import play.api.Configuration
import play.api.cache.{ NamedCache, SyncCacheApi }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

final class WebSocketActor @Inject()(
    @Assisted("out") out: ActorRef,
    jobActorAccess: JobActorAccess,
    userSessions: UserSessionService,
    constants: ConstantsV2,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
    @Assisted("sessionID") sessionID: String,
    toolConfig: ToolConfig,
    @Named("clusterSubscriber") clusterSubscriber: ActorRef
)(implicit ec: ExecutionContext, config: Configuration)
    extends Actor
    with ActorLogging {

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def preStart(): Unit = {
    context.system.eventStream.subscribe(self, classOf[UpdateLoad])
    context.system.eventStream.subscribe(self, classOf[MaintenanceAlert])
    userSessions.getUserBySessionID(sessionID).foreach {
      case Some(user) =>
        wsActorCache.get[List[ActorRef]](user.userID) match {
          case Some(wsActors) =>
            val actorSet = (wsActors: List[ActorRef]).::(self)
            wsActorCache.set(user.userID, actorSet)
          case None =>
            wsActorCache.set(user.userID, List(self))
        }
        val loadFuture = clusterSubscriber ? SGELoad.Ask
        loadFuture.mapTo[UpdateLoad].map { response =>
          out ! JsonObject(
            "type" -> Json.fromString("UpdateLoad"),
            "load" -> Json.fromDoubleOrNull(response.load)
          ).asJson
        }
      case None =>
        self ! PoisonPill
    }
  }

  override def postStop(): Unit = {
    userSessions
      .getUserBySessionID(sessionID)
      .map(_.foreach { user =>
        wsActorCache.get[List[ActorRef]](user.userID).foreach { wsActors =>
          val actorSet: List[ActorRef] = wsActors: List[ActorRef]
          val newActorSet              = actorSet.filterNot(_ == self)
          wsActorCache.set(user.userID, newActorSet)
        }
      })
    context.system.eventStream.unsubscribe(self, classOf[UpdateLoad])
    context.system.eventStream.unsubscribe(self, classOf[MaintenanceAlert])
    log.info(s"[WSActor] Websocket closed for session $sessionID")
  }

  private def active(sid: String): Receive = {

    case json: Json =>
      userSessions.getUserBySessionID(sid).foreach {
        case Some(user) =>
          json.hcursor.get[String]("type").toOption.foreach {

            case "SET_JOB_WATCHED" =>
              for {
                jobID   <- json.hcursor.get[String]("jobID")
                watched <- json.hcursor.get[Boolean]("watched")
              } yield {
                jobActorAccess.sendToJobActor(
                  jobID,
                  if (watched) AddToWatchlist(jobID, user.userID)
                  else RemoveFromWatchlist(jobID, user.userID)
                )
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
                log.info(s"[WSActor] Ping of session $sid is ${ping}ms.")
              }

            case other: String =>
              log.warning(s"[WSActor] No action for $other.")
          }
        case None =>
          self ! PoisonPill
      }

    case PushJob(job: Job) =>
      userSessions.getUserBySessionID(sid).foreach {
        case Some(user) =>
          out ! JsonObject(
            "namespace" -> Json.fromString("jobs"),
            "mutation"  -> Json.fromString("SOCKET_UpdateJob"),
            "job"       -> job.jsonPrepare(toolConfig, user).asJson
          ).asJson
        case None =>
          self ! PoisonPill
      }
    case ShowNotification(title: String, body: String) =>
      out ! JsonObject(
        "mutation" -> Json.fromString("SOCKET_ShowNotification"),
        "title"    -> Json.fromString(title),
        "body"     -> Json.fromString(body)
      ).asJson

    case ShowJobNotification(jobID: String, title: String, body: String) =>
      out ! JsonObject(
        "mutation" -> Json.fromString("SOCKET_ShowJobNotification"),
        "jobID"    -> Json.fromString(jobID),
        "title"    -> Json.fromString(title),
        "body"     -> Json.fromString(body)
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
            "mutation" -> Json.fromString("SOCKET_WatchLogFile"),
            "jobID"    -> Json.fromString(job.jobID),
            "lines"    -> Json.fromString(lines)
          ).asJson
          source.close()
        }
      }

    case UpdateLoad(load: Double) =>
      out ! JsonObject(
        "mutation" -> Json.fromString("SOCKET_UpdateLoad"),
        "load"     -> Json.fromDoubleOrNull(load)
      ).asJson

    case ClearJob(jobID: String, deleted: Boolean) =>
      out ! JsonObject(
        "namespace" -> Json.fromString("jobs"),
        "mutation"  -> Json.fromString("SOCKET_ClearJob"),
        "jobID"     -> Json.fromString(jobID),
        "deleted"   -> Json.fromBoolean(deleted)
      ).asJson

    case ChangeSessionID(newSid: String) =>
      context.become(active(newSid))
      out ! JsonObject("mutation" -> Json.fromString("SOCKET_Login")).asJson

    case LogOut() =>
      out ! JsonObject("mutation" -> Json.fromString("SOCKET_Logout")).asJson

    case MaintenanceAlert(maintenanceMode) =>
      out ! JsonObject(
        "mutation"        -> Json.fromString("SOCKET_MaintenanceAlert"),
        "maintenanceMode" -> Json.fromBoolean(maintenanceMode)
      ).asJson
  }

  override def receive = LoggingReceive {
    active(sessionID)
  }
}

object WebSocketActor {

  case class MaintenanceAlert(maintenanceMode: Boolean)

  trait Factory {
    def apply(@Assisted("sessionID") sessionID: String, @Assisted("out") out: ActorRef): Actor
  }

}
