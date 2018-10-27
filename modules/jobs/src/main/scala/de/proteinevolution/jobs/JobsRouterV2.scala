package de.proteinevolution.jobs

import de.proteinevolution.jobs.controllers.JobGetController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class JobsRouterV2 @Inject()(
    jobGetController: JobGetController
) extends SimpleRouter {
  override def routes: Routes = {
    case GET(p"/") => jobGetController.listJobs
    case GET(p"/$jobID") => jobGetController.loadJob(jobID)
  }
}
