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

import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.tools.ToolConfig
import javax.inject.{ Inject, Singleton }
import play.api.mvc._
import play.api.{ Environment, Logging }

import scala.concurrent.ExecutionContext

@Singleton
final class Application @Inject()(
    toolConfig: ToolConfig,
    userSessions: UserSessionService,
    cc: ControllerComponents,
    environment: Environment,
    assetsFinder: AssetsFinder,
    userAction: UserAction
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc)
    with Logging {

  def index(message: String = ""): Action[AnyContent] = userAction { implicit request =>
    logger.info(InetAddress.getLocalHost.getHostName + "\n" + request.user.toString)
    Ok(
      views.html.main(assetsFinder, toolConfig.values.values.toSeq.sortBy(_.toolNameLong), message, "", environment)
    ).withSession(userSessions.sessionCookie(request, request.user.sessionID.get))
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
