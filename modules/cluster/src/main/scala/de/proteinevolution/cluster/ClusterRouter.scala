package de.proteinevolution.cluster

import de.proteinevolution.cluster.controllers.ClusterController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class ClusterRouter @Inject()(ctrl: ClusterController) extends SimpleRouter {

  override lazy val routes: Routes = {
    case GET(p"/load") => ctrl.getLoad
  }

}
