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

package controllers

import java.net.InetAddress

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.tel.env.Env
import de.proteinevolution.tools.ToolConfig
import javax.inject.{ Inject, Singleton }
import org.webjars.play.WebJarsUtil
import play.api.mvc._
import play.api.{ Configuration, Environment, Logging }

import scala.concurrent.ExecutionContext

@Singleton
final class Application @Inject()(
    webJarsUtil: WebJarsUtil,
    toolConfig: ToolConfig,
    userSessions: UserSessions,
    cc: ControllerComponents,
    env: Env,
    environment: Environment,
    assetsFinder: AssetsFinder,
    config: Configuration
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc)
    with Logging {

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

    env.configure(s"HOSTNAME", config.get[String]("host_name"))

    environment.mode match {

      case play.api.Mode.Prod =>
        val port = "9000"
        env.configure("PORT", port)

      case _ =>
        val port = request.host.slice(request.host.indexOf(":") + 1, request.host.length)
        env.configure("PORT", port)
    }
  }

}
