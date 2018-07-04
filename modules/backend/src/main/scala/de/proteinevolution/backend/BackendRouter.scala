package de.proteinevolution.backend

import de.proteinevolution.backend.controllers.BackendController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class BackendRouter @Inject()(ctrl: BackendController) extends SimpleRouter {

  private lazy val backendRoutes: Routes = {
    case GET(p"/index")        => ctrl.index
    case GET(p"/statistics")   => ctrl.statistics
    case GET(p"/runusersweep") => ctrl.runUserSweep
    case GET(p"/runjobsweep")  => ctrl.runJobSweep
    case GET(p"/users")        => ctrl.users
    case POST(p"/users")       => ctrl.users
    case POST(p"/maintenance") => ctrl.maintenance
  }

  override lazy val routes: Routes = {
    backendRoutes
  }

}
