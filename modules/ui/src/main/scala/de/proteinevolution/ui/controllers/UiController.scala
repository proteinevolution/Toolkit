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

package de.proteinevolution.ui.controllers

import controllers.AssetsFinder
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.tools.ToolConfig
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

@Singleton
class UiController @Inject()(
    cc: ControllerComponents,
    toolConfig: ToolConfig,
    assets: AssetsFinder
) extends ToolkitController(cc) {

  def static(static: String): Action[AnyContent] = Action { implicit request =>
    static match {
      // Frontend tools
      case "reformat" =>
        Ok(views.html.tools.forms.reformat(assets))
      case _ =>
        NotFound(views.html.errors.pagenotfound(assets))
    }
  }

  def getTool(toolname: String) = Action {
    toolConfig.values.get(toolname) match {
      case Some(tool) => Ok(tool.toolForm.asJson)
      case None       => NotFound
    }
  }

  def recentUpdates = Action {
    Ok(views.html.elements.recentupdates())
  }

}
