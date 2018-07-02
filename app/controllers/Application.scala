package controllers

import java.net.InetAddress

import javax.inject.{ Inject, Singleton }
import akka.actor.{ ActorSystem, Props }
import akka.stream.Materializer
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.message.actors.WebSocketActor
import de.proteinevolution.services.ToolConfig
import de.proteinevolution.tel.TEL
import de.proteinevolution.tel.env.Env
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.{ Configuration, Environment, Logger }
import org.webjars.play.WebJarsUtil
import play.api.routing.{ JavaScriptReverseRoute, JavaScriptReverseRouter }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class Application @Inject()(
    webJarsUtil: WebJarsUtil,
    webSocketActorFactory: WebSocketActor.Factory,
    toolConfig: ToolConfig,
    userSessions: UserSessions,
    env: Env,
    cc: ControllerComponents,
    config: Configuration,
    environment: Environment,
    assetsFinder: AssetsFinder
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
      Future.successful {
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
    origin.contains(TEL.hostname + ":" + TEL.port) || origin.contains("tuebingen.mpg.de") || origin.contains(
      "tue.mpg.de"
    )
  }

  def index(message: String = ""): Action[AnyContent] = Action.async { implicit request =>
    //generateStatisticsDB
    environment.mode match {
      case play.api.Mode.Prod =>
        val port     = "9000"
        val hostname = "rye"
        env.configure("PORT", port)
        env.configure("HOSTNAME", hostname)
        TEL.port = port
        TEL.hostname = hostname
        logger.info(s"[CONFIG:] running on port ${TEL.port} in mode: play.api.Mode.Prod")
      case _ =>
        val port     = request.host.slice(request.host.indexOf(":") + 1, request.host.length)
        val hostname = request.host.slice(0, request.host.indexOf(":"))
        env.configure("PORT", port)
        env.configure("HOSTNAME", "olt")
        TEL.port = port
        TEL.hostname = hostname
        logger.info(s"[CONFIG:] running on port ${TEL.port} in mode: play.api.Mode.Dev")
    }
    userSessions.getUser.map { user =>
      logger.info(InetAddress.getLocalHost.getHostName + "\n" + user.toString)
      Ok(
        views.html.main(assetsFinder,
                        webJarsUtil,
                        toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                        message,
                        "",
                        environment)
      ).withSession(userSessions.sessionCookie(request, user.sessionID.get))
    }
  }

  // Routes are handled by Mithril, redirect.
  def showTool(toolName: String): Action[AnyContent] = Action { implicit request =>
    PermanentRedirect(s"/#/tools/$toolName")
  }

  def showJob(idString: String): Action[AnyContent] = Action { implicit request =>
    PermanentRedirect(s"/#/jobs/$idString")
  }

  def static(static: String): Action[AnyContent] = Action { implicit request =>
    PermanentRedirect(s"/#/$static")
  }

  def maintenance: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      if (user.isSuperuser) {
        //clusterMonitor ! Multicast TODO put somewhere else
        Ok
      } else {
        NotFound
      }
    }
  }

  val robots = Action { _ =>
    Ok(
      "User-agent: *\nAllow: /\nDisallow: /#/jobmanager/\nDisallow: /#/jobs/\nSitemap: https://toolkit.tuebingen.mpg.de/sitemap.xml"
    )
  }

  /** Exposes callback function in order to configure the websocket connection dependent on the protocol */
  def wsConfig: Action[AnyContent] = Action { implicit request =>
    val callBack = """function() {
                       |          return _wA({method:"GET", url:"/" + "ws"})
                       |        }""".stripMargin.trim
    Ok(JavaScriptReverseRouter("jsRoutes")(JavaScriptReverseRoute("controllers.Application.ws", callBack)))
      .as("text/javascript")
      .withHeaders(CACHE_CONTROL -> "max-age=31536000")
  }

  def recentUpdates = Action {
    Ok(views.html.elements.recentupdates())
  }

}
