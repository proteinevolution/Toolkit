package controllers

import java.net.InetAddress

import scala.concurrent.ExecutionContext

@Singleton
final class Application @Inject()(
    webJarsUtil: WebJarsUtil,
    toolConfig: ToolConfig,
    userSessions: UserSessions,
    cc: ControllerComponents,
    environment: Environment,
    assetsFinder: AssetsFinder
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  private val logger = Logger(this.getClass)

  def index(message: String = ""): Action[AnyContent] = Action.async { implicit request =>
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

}
