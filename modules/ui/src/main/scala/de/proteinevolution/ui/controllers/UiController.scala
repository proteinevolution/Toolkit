package de.proteinevolution.ui.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.services.ToolConfig
import io.circe.generic.auto._
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.mvc.ControllerComponents

@Singleton
class UiController @Inject()(
    cc: ControllerComponents,
    toolConfig: ToolConfig
) extends ToolkitController(cc) {

  def getToolParameters(toolname: String) = Action {
    toolConfig.values.get(toolname) match {
      case Some(tool) => Ok(tool.toolParameterForm.asJson)
      case None       => NotFound
    }
  }

  def getTools = Action {
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
    Ok(sorted.asJson)
  }

  def getToolsVersion = Action {
    Ok(toolConfig.version)
  }

}
