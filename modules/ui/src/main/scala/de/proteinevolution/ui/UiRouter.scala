package de.proteinevolution.ui

import de.proteinevolution.ui.controllers.UiController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class UiRouter @Inject()(uiController: UiController) extends SimpleRouter {

  override lazy val routes: Routes = {
    case GET(p"/tool/$toolName") => uiController.getTool(toolName)
    case GET(p"/static/$static") => uiController.static(static)
    case GET(p"/recent/updates") => uiController.recentUpdates
  }

}
