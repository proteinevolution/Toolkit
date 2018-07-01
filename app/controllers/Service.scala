package controllers

import javax.inject.{ Inject, Singleton }
import play.api.mvc._

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.services.ToolConfig
import play.api.libs.json._

@Singleton
final class Service @Inject()(
    toolConfig: ToolConfig,
    cc: ControllerComponents,
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
}
