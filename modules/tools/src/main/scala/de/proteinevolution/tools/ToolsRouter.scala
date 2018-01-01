package de.proteinevolution.tools

import javax.inject.{ Inject, Singleton }

import de.proteinevolution.tools.controllers.{ HHController, ProcessController }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class ToolsRouter @Inject()(
    hhController: HHController,
    processController: ProcessController
) extends SimpleRouter {

  def routes: Routes = {
    case POST(p"/loadHits/$jobID")                    => hhController.loadHits(jobID)
    case GET(p"/dataTable/$jobID")                    => hhController.dataTable(jobID)
    case POST(p"/forwardAlignment/$jobID/$mode")      => processController.forwardAlignment(jobID, mode)
    case GET(p"/templateAlignment/$jobID/$accession") => processController.templateAlignment(jobID, accession)
  }

}
