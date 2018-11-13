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
