package de.proteinevolution.help

import de.proteinevolution.help.controllers.HelpController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class HelpRouter @Inject()(ctrl: HelpController) extends SimpleRouter {

  override lazy val routes: Routes = {
    case GET(p"/$toolName") => ctrl.getHelp(toolName)
  }

}
