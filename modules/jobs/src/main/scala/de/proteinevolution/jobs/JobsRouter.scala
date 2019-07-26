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

import de.proteinevolution.jobs.controllers._
import de.proteinevolution.jobs.services.ForwardModeExtractor
import javax.inject.{Inject, Singleton}
import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

@Singleton
class JobsRouter @Inject()(
    submissionController: SubmissionController,
    clusterApiController: ClusterApiController,
    jobGetController: JobGetController,
    hhController: HHController,
    processController: ProcessController,
    resultsController: ResultsController,
    fileController: FileController,
    forwardModalController: ForwardModalController
) extends SimpleRouter
    with ForwardModeExtractor {

  private lazy val getRoutes: Routes = {
    case GET(p"/")                        => jobGetController.getAllJobs
    case GET(p"/suggest/$query")          => jobGetController.suggestJobsForQuery(query.trim)
    case GET(p"/$jobID")                  => jobGetController.loadJob(jobID)
    case GET(p"/$jobID/start")            => submissionController.startJob(jobID)
    case GET(p"/check/hash/$jobID")       => jobGetController.checkHash(jobID)
    case GET(p"/check/job-id/$newJobID/") => submissionController.checkJobID(newJobID)
  }

  private lazy val submissionRoutes: Routes = {
    case POST(p"/" ? q"toolName=$toolName")  => submissionController.submitJob(toolName)
    case PUT(p"/$jobID")                     => submissionController.changeJob(jobID)
    case DELETE(p"/$jobID")                  => submissionController.delete(jobID)
    case POST(p"/frontend/submit/$toolName") => submissionController.frontend(toolName)
  }

  private lazy val clusterApiRoutes: Routes = {
    case PUT(p"/status/$status/$jobID/$key") => clusterApiController.setJobStatus(status, jobID, key)
    case PUT(p"/sge/$jobID/$sgeID/$key")     => clusterApiController.setSgeId(jobID, sgeID, key)
  }

  private lazy val resultRoutes: Routes = {
    case GET(p"/$jobID/results/alignments/" ? q_o"start=${int(start)}" & q_o"end=${int(end)}") =>
      resultsController.loadAlignmentHits(jobID, start, end)
    case GET(p"/$jobID/results/hh-alignments/" ? q_o"start=${int(start)}" & q_o"end=${int(end)}") =>
      hhController.loadAlignments(jobID, start, end)
    case GET(p"/$jobID/results/files/$filename") => fileController.file(filename = filename, jobID = jobID)
    case GET(p"/$jobID/results/hits/" ? q_o"start=${int(start)}" & q_o"end=${int(end)}"
      & q_o"filter=${filter}" & q_o"sortBy=${sortBy}"& q_o"desc=${bool(desc)}") =>
      hhController.loadHits(jobID, start, end, filter, sortBy, desc)
    case GET(p"/$jobID/results/") => resultsController.loadResults(jobID)

    case GET(p"/getStructure/$filename") => fileController.getStructureFile(filename)
    case POST(p"/forwardAlignment/$jobID/${forwardModeExtractor(mode) }") =>
      processController.forwardAlignment(jobID, mode)
    case GET(p"/templateAlignment/$jobID/$accession") => processController.templateAlignment(jobID, accession)
    case POST(p"/alignment/getAln/$jobID")            => resultsController.getAln(jobID)
    case GET(p"/forward/modal/$toolName/$modalType") =>
      forwardModalController.getForwardModalOptions(modalType, toolName)
  }

  override def routes: Routes = {
    submissionRoutes.orElse(clusterApiRoutes).orElse(getRoutes).orElse(resultRoutes)
  }

}
