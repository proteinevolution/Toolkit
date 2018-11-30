package de.proteinevolution.jobs

import de.proteinevolution.jobs.controllers.{ JobGetController, SubmissionController }
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class JobsRouterV2 @Inject()(
    submissionController: SubmissionController,
    jobGetController: JobGetController
) extends SimpleRouter {
  override def routes: Routes = {
    case POST(p"/" ? q"toolName=$toolName") => submissionController.submitJob(toolName)
    case GET(p"/$jobID")                    => jobGetController.loadJob(jobID)
    case GET(p"/")                          => jobGetController.listJobs
  }
}
