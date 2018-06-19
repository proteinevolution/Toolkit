package controllers

import java.net.InetAddress

import javax.inject.{ Inject, Named, Singleton }
import actors.ClusterMonitor.Multicast
import actors.WebSocketActor
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.stream.Materializer
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.ToolkitController
import models.tools.ToolFactory
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
    @Named("clusterMonitor") clusterMonitor: ActorRef,
    webSocketActorFactory: WebSocketActor.Factory,
    toolFactory: ToolFactory,
    userSessions: UserSessions,
    env: Env,
    cc: ControllerComponents,
    config: Configuration,
    environment: Environment,
    assetsFinder: AssetsFinder
)(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  private[this] val blacklist = config.get[Seq[String]]("banned.ip")

  /**
   * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
   * Future[Flow], which is required internally.
   *
   * @return a fully realized websocket.
   */
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
            logger.error("Cannot create websocket", e)
            val jsError = Json.obj("error" -> "Cannot create websocket")
            val result  = InternalServerError(jsError)
            Left(result)
        }
    case rejected =>
      logger.error(s"Request $rejected failed same origin check")
      Future.successful {
        Left(Ok("forbidden"))
      }
  }

  /**
   * Checks that the WebSocket comes from the same origin.  This is necessary to protect
   * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
   *
   * See https://tools.ietf.org/html/rfc6455#section-1.3 and
   * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
   */
  def sameOriginCheck(rh: RequestHeader): Boolean = {
    if (environment.mode == play.api.Mode.Test)
      true
    else {
      rh.headers.get("Origin") match {
        case Some(originValue) if originMatches(originValue) && !blacklist.contains(rh.remoteAddress) =>
          logger.debug(s"originCheck: originValue = $originValue")
          true
        case Some(badOrigin) =>
          logger.error(
            s"originCheck: rejecting request because Origin header value $badOrigin is not in the same origin"
          )
          false
        case None =>
          logger.error("originCheck: rejecting request because no Origin header found")
          false
      }
    }
  }

  /**
   * Returns true if the value of the Origin header contains an acceptable value.
   */
  def originMatches(origin: String): Boolean = {
    origin.contains(TEL.hostname + ":" + TEL.port) || origin.contains("tuebingen.mpg.de") || origin.contains(
      "tue.mpg.de"
    )
  }

  /**
   * Handles the request of the index page of the toolkit. This will assign a session to the User if
   * not already present.
   * Currently the index controller will assign a session id to the user for identification purpose.
   */
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
        if (!hostname.startsWith("olt")) {
          env.configure("headLessMode", "true")
        }
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
                        toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
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
        clusterMonitor ! Multicast
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

}
