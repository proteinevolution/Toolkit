package controllers

import java.net.InetAddress

import javax.inject.{ Inject, Singleton }
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.tel.env.Env
import de.proteinevolution.tools.ToolConfig
import play.api.mvc._
import play.api.{ Environment, Logger }
import org.webjars.play.WebJarsUtil

import scala.concurrent.ExecutionContext

@Singleton
final class Application @Inject()(
    webJarsUtil: WebJarsUtil,
    toolConfig: ToolConfig,
    userSessions: UserSessions,
    cc: ControllerComponents,
    env: Env,
    environment: Environment,
    assetsFinder: AssetsFinder
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  def index(message: String = ""): Action[AnyContent] = Action.async { implicit request =>
    configEnv(request)
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

  val robots = Action { _ =>
    Ok(
      "User-agent: *\nAllow: /\nDisallow: /#/jobmanager/\nDisallow: /#/jobs/\nSitemap: https://toolkit.tuebingen.mpg.de/sitemap.xml"
    )
  }

  private def configEnv(request: Request[AnyContent]): Unit = {
    environment.mode match {
      case play.api.Mode.Prod =>
        val port     = "9000"
        val hostname = "rye"
        env.configure("PORT", port)
        env.configure("HOSTNAME", hostname)
      case _ =>
        val port = request.host.slice(request.host.indexOf(":") + 1, request.host.length)
        //val hostname = request.host.slice(0, request.host.indexOf(":"))
        env.configure("PORT", port)
        env.configure("HOSTNAME", "olt")
    }
  }

}
