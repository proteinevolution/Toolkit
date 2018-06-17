package de.proteinevolution.jobs

import de.proteinevolution.jobs.controllers.SubmissionController
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class JobsRouter @Inject()(submissionController: SubmissionController) extends SimpleRouter {

  private lazy val submissionRoutes: Routes = {
    case GET(p"/test") => submissionController.test()
  }

  override def routes: Routes = {
    submissionRoutes
  }

}
