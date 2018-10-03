package de.proteinevolution.ui.controllers

import controllers.AssetsFinder
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.services.ToolConfig
import javax.inject.{ Inject, Singleton }
import play.api.libs.json.Json
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
      case Some(tool) => Ok(Json.toJson(tool.toolForm))
      case None       => NotFound
    }
  }

  def getTools = Action {
    val sections = toolConfig.sections
    val sorted = toolConfig.values.toSeq
      .filter(t => sections.contains(t._2.section))
      .sortWith((l, r) => {
        val lTool = l._2
        val rTool = r._2
        if (lTool.section == rTool.section)
          lTool.order < rTool.order
        else
          sections.getOrElse(lTool.section, 0) < sections.getOrElse(rTool.section, 0)
      })
      .map {
        case (_, v) =>
          v.toolFormSimple
      }
    Ok(Json.toJson(sorted))
  }

  def getToolsVersion = Action {
    Ok(toolConfig.version)
  }

  def recentUpdates = Action {
    Ok(views.html.elements.recentupdates())
  }

}
