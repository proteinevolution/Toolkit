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

package de.proteinevolution.jobs.services

import cats.data.EitherT
import cats.implicits._
import de.proteinevolution.common.models.ToolName._
import de.proteinevolution.common.models.database.jobs.JobState.Done
import de.proteinevolution.common.models.{ConstantsV2, ToolName}
import de.proteinevolution.jobs.db.ResultFileAccessor
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.jobs.models.resultviews._
import de.proteinevolution.jobs.results._
import de.proteinevolution.tools.ToolConfig
import io.circe.{DecodingFailure, Json}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

/**
* This class is only there as reference! It should not be used anymore.
 */
@Singleton
final class ResultViewFactory @Inject()(
    constants: ConstantsV2,
    toolConfig: ToolConfig,
    resultFiles: ResultFileAccessor
)(implicit ec: ExecutionContext) {

  def apply(toolName: String, jobId: String): EitherT[Future, _, ResultView] = {
    if (ToolName(toolName).hasJson) {
      for {
        result <- EitherT.liftF(resultFiles.getResults(jobId))
        view   <- EitherT.fromEither[Future](getResultViewsWithJson(toolName, jobId, result))
      } yield view
    } else {
      EitherT.pure(getResultViewsWithoutJson(toolName, jobId))
    }
  }

  def getJobViewsForJob(job: Job): Future[Seq[String]] = job.status match {
    case Done =>
      apply(job.tool, job.jobID).value.map {
        case Right(r) => r.tabs.keys.toSeq
        case Left(_) =>
          Nil
      }
    case _ => Future.successful(Nil)
  }

  private def getResultViewsWithoutJson(toolName: String, jobId: String): ResultView = {
    (ToolName(toolName): @unchecked) match {
      case HHPRED_MANUAL       => HHPredManual(jobId, constants)
    }
  }

  private def getResultViewsWithJson(
      toolName: String,
      jobId: String,
      json: Json
  ): Either[DecodingFailure, ResultView] = {
    (ToolName(toolName): @unchecked) match {
      case PSIBLAST =>
        for {
          result <- json.as[PSIBlastResult]
        } yield PsiBlastResultView(jobId, result, toolConfig, constants)
      case HHPRED =>
        for {
          result    <- json.as[HHPredResult]
          alignment <- json.hcursor.downField("querytemplate").as[AlignmentResult]
          reduced   <- json.hcursor.downField("reduced").as[AlignmentResult]
        } yield HHPredResultView(jobId, result, alignment, reduced, toolConfig, constants)
      case HHPRED_ALIGN =>
        for {
          result <- json.as[HHPredResult]
        } yield HHPredAlignResultView(jobId, result, toolConfig, constants)
    }
  }

}
