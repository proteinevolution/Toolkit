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

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.tools.ToolConfig
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}

@Singleton
class UiController @Inject()(
    cc: ControllerComponents,
    toolConfig: ToolConfig
) extends ToolkitController(cc) {

  def getToolParameters(toolname: String): Action[AnyContent] = Action {
    toolConfig.values.get(toolname) match {
      case Some(tool) => Ok(tool.toolParameterForm.asJson)
      case None       => NotFound
    }
  }

  def getTools: Action[AnyContent] = Action {
    val sorted = toolConfig.values.toSeq
      .sortWith((l, r) => {
        val lTool = l._2
        val rTool = r._2
        lTool.order < rTool.order
      })
      .map {
        case (_, v) =>
          v.toolFormSimple
      }
    Ok(sorted.asJson.printWith(Printer.noSpaces.copy(dropNullValues = true)))
  }

  def getToolsVersion: Action[AnyContent] = Action {
    Ok(toolConfig.version)
  }

}
