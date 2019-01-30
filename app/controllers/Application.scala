package controllers

import java.net.InetAddress

import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.tools.ToolConfig
import javax.inject.{ Inject, Singleton }
import org.webjars.play.WebJarsUtil
import play.api.mvc._
import play.api.{ Environment, Logger }

import scala.concurrent.ExecutionContext

@Singleton
final class Application @Inject()(
    webJarsUtil: WebJarsUtil,
    toolConfig: ToolConfig,
    userSessions: UserSessionService,
    cc: ControllerComponents,
    environment: Environment,
    assetsFinder: AssetsFinder,
    userAction: UserAction
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  def index(message: String = ""): Action[AnyContent] = userAction { implicit request =>
    logger.info(InetAddress.getLocalHost.getHostName + "\n" + request.user.toString)
    Ok(
      views.html.main(assetsFinder,
                      webJarsUtil,
                      toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                      message,
                      "",
                      environment)
    ).withSession(userSessions.sessionCookie(request))
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

  val robots: Action[AnyContent] = Action { _ =>
    Ok(
      "User-agent: *\nAllow: /\nDisallow: /#/jobmanager/\nDisallow: /#/jobs/\nSitemap: https://toolkit.tuebingen.mpg.de/sitemap.xml"
    )
  }

}
