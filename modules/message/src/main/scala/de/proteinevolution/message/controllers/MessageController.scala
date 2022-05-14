/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

package de.proteinevolution.message.controllers

import akka.actor.{ ActorSystem, Props }
import akka.stream.Materializer
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.message.actors.WebSocketActor
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Singleton }
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.{ Configuration, Environment, Logging }

import scala.concurrent.ExecutionContext

@Singleton
class MessageController @Inject(
    cc: ControllerComponents,
    userSessions: UserSessionService,
    environment: Environment,
    config: Configuration,
    webSocketActorFactory: WebSocketActor.Factory
)(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext)
    extends ToolkitController(cc)
    with Logging {

  import de.proteinevolution.message.helpers.CirceFlowTransformer._

  def ws: WebSocket = WebSocket.acceptOrResult[Json, Json] {
    case rh if sameOriginCheck(rh) =>
      logger.info("Creating new WebSocket. ip: " + rh.remoteAddress.toString + ", with sessionId: " + rh.session)
      userSessions
        .getUser(rh)
        .map { user =>
          Right(ActorFlow.actorRef(out => Props(webSocketActorFactory(user.sessionID.get, out))))
        }
        .recover { case e: Exception =>
          logger.warn("Cannot create websocket", e)
          val jsError = JsonObject("error" -> Json.fromString("Cannot create websocket"))
          Left(BadRequest(jsError.asJson))
        }
    case rejected =>
      logger.warn(s"Request $rejected failed same origin check")
      fuccess {
        Left(Forbidden)
      }
  }

  private def sameOriginCheck(rh: RequestHeader): Boolean = {
    if (environment.mode != play.api.Mode.Prod)
      true
    else {
      rh.headers.get("Origin") match {
        case Some(originValue)
            if originMatches(originValue) && !config.get[Seq[String]]("banned.ip").contains(rh.remoteAddress) =>
          logger.debug(s"originCheck: originValue = $originValue")
          true
        case Some(badOrigin) =>
          logger.warn(
            s"originCheck: rejecting request because Origin header value $badOrigin is not in the same origin"
          )
          false
        case None =>
          logger.warn("originCheck: rejecting request because no Origin header found")
          false
      }
    }
  }

  private def originMatches(origin: String): Boolean = {
    origin.contains("http://localhost") || origin.contains(s"http://${config.get[String]("host_name")}") || origin
      .contains("tuebingen.mpg.de") || origin.contains(
      "tue.mpg.de"
    )
  }

}
