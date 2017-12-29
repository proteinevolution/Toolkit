package de.proteinevolution.tools

import javax.inject.{ Inject, Singleton }

import de.proteinevolution.tools.controllers.HHController
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class ToolsRouter @Inject()(
    hhController: HHController
) extends SimpleRouter {

  def routes: Routes = {

    case POST(p"/loadHits")        => hhController.loadHits
    case GET(p"/dataTable/$jobID") => hhController.dataTable(jobID)
    case POST(p"/eval")            => hhController.eval
    case POST(p"/evalFull")        => hhController.evalFull
    case POST(p"/full")            => hhController.full
    case POST(p"/aln")             => hhController.aln

  }

}
