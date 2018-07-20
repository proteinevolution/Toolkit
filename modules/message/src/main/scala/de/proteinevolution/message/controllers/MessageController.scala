package de.proteinevolution.message.controllers

import akka.actor.{ ActorSystem, Props }
import akka.stream.Materializer
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.message.actors.WebSocketActor
import javax.inject.Inject
import play.api.{ Configuration, Environment, Logger }
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.ExecutionContext

class MessageController @Inject()(
    cc: ControllerComponents,
    userSessions: UserSessions,
    environment: Environment,
    config: Configuration,
    webSocketActorFactory: WebSocketActor.Factory
)(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  def ws: WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {
    case rh if sameOriginCheck(rh) =>
      logger.info("Creating new WebSocket. ip: " + rh.remoteAddress.toString + ", with sessionId: " + rh.session)
      userSessions
        .getUser(rh)
        .map { user =>
          Right(ActorFlow.actorRef(out => Props(webSocketActorFactory(user.sessionID.get, out))))
        }
        .recover {
          case e: Exception =>
            logger.warn("Cannot create websocket", e)
            val jsError = Json.obj("error" -> "Cannot create websocket")
            Left(BadRequest(jsError))
        }
    case rejected =>
      logger.warn(s"Request $rejected failed same origin check")
      fuccess {
        Left(Forbidden)
      }
  }

  private def sameOriginCheck(rh: RequestHeader): Boolean = {
    if (environment.mode == play.api.Mode.Test)
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
    origin.contains("http://localhost") || origin.contains("http://olt") || origin.contains("tuebingen.mpg.de") || origin
      .contains(
        "tue.mpg.de"
      )
  }

}
