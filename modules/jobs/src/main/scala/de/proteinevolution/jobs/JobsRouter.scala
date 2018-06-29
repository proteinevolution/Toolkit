package de.proteinevolution.jobs

import de.proteinevolution.jobs.controllers.{ ClusterApiController, JobGetController, SubmissionController }
import javax.inject.{ Inject, Singleton }
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class JobsRouter @Inject()(
    submissionController: SubmissionController,
    clusterApiController: ClusterApiController,
    jobGetController: JobGetController
) extends SimpleRouter {

  private lazy val getRoutes: Routes = {
    case GET(p"/")                  => jobGetController.listJobs
    case GET(p"/load/$jobID")       => jobGetController.loadJob(jobID)
    case GET(p"/check/hash/$jobID") => jobGetController.checkHash(jobID)
    case GET(p"/manager/jobs")      => jobGetController.jobManagerListJobs
  }

  private lazy val submissionRoutes: Routes = {
    case POST(p"/" ? q"toolName=$toolName")  => submissionController.submitJob(toolName)
    case DELETE(p"/$jobID")                  => submissionController.delete(jobID)
    case POST(p"/start/$jobID")              => submissionController.startJob(jobID)
    case POST(p"/frontend/submit/$toolName") => submissionController.frontend(toolName)
  }

  private lazy val clusterApiRoutes: Routes = {
    case PUT(p"/status/$status/$jobID/$key") => clusterApiController.setJobStatus(status, jobID, key)
    case PUT(p"/sge/$jobID/$sgeID/$key")     => clusterApiController.setSgeId(jobID, sgeID, key)
  }

  override def routes: Routes = {
    submissionRoutes.orElse(clusterApiRoutes).orElse(getRoutes)
  }

}
