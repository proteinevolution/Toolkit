/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    case GET(p"/recent")            => jobGetController.recentJob
    case GET(p"/load/$jobID")       => jobGetController.loadJob(jobID)
    case GET(p"/suggest/$query")    => jobGetController.suggestJobsForJobId(query)
    case GET(p"/check/hash/$jobID") => jobGetController.checkHash(jobID)
    case GET(p"/manager/jobs")      => jobGetController.jobManagerListJobs
  }

  private lazy val submissionRoutes: Routes = {
    case POST(p"/" ? q"toolName=$toolName")  => submissionController.submitJob(toolName)
    case GET(p"/check/job-id/$newJobID/")    => submissionController.checkJobID(newJobID)
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
