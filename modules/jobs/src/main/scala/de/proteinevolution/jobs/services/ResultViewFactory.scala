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
      case FORMATSEQ           => FormatSeqResultView(jobId, toolConfig)
      case CLANS               => ClansResultView(jobId)
      case MARCOIL             => MarcoilResultView(jobId, toolConfig)
      case DEEPCOIL            => DeepCoilResultView(jobId, toolConfig, constants)
      case PCOILS              => PcoilsResultView(jobId, constants)
      case REPPER              => RepperResultView(jobId, constants)
      case MODELLER            => ModellerResultView(jobId, constants)
      case HHPRED_MANUAL       => HHPredManual(jobId, constants)
      case HHREPID             => HHrepIdResultView(jobId, constants)
      case ALI2D               => Ali2DResultView(jobId, constants)
      case ALN2PLOT            => Aln2PlotResultView(jobId)
      case MMSEQS2             => MMSeqsResultView(jobId, toolConfig)
      case RETSEQ              => RetSeqResultView(jobId, constants, toolConfig)
      case SAMCC               => SamCCResultView(jobId)
      case SIXFRAMETRANSLATION => SixFrameTranslationResultView(jobId)
      case BACKTRANS           => BackTransResultView(jobId)
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
      case TPRPRED =>
        for {
          result <- TPRPredResult.tprpredDecoder(jobId, json)
        } yield TprPredResultView(jobId, result)
      case HHBLITS =>
        for {
          result    <- json.as[HHBlitsResult]
          alignment <- json.hcursor.downField("querytemplate").as[AlignmentResult]
          reduced   <- json.hcursor.downField("reduced").as[AlignmentResult]
        } yield HHBlitsResultView(jobId, result, alignment, reduced, toolConfig, constants)
      case HMMER =>
        for {
          result <- json.as[HmmerResult]
        } yield HmmerResultView(jobId, result, toolConfig, constants)
      case HHPRED =>
        for {
          result    <- json.as[HHPredResult]
          alignment <- json.hcursor.downField("querytemplate").as[AlignmentResult]
          reduced   <- json.hcursor.downField("reduced").as[AlignmentResult]
        } yield HHPredResultView(jobId, result, alignment, reduced, toolConfig, constants)
      case HHOMP =>
        for {
          result <- json.as[HHompResult]
        } yield HHompResultView(jobId, result, constants, toolConfig)
      case HHPRED_ALIGN =>
        for {
          result <- json.as[HHPredResult]
        } yield HHPredAlignResultView(jobId, result, toolConfig, constants)
      case SEQ2ID =>
        for {
          result <- json.as[Unchecked]
        } yield Seq2IdResultView(jobId, result)
      case HHFILTER =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield HHFilterResultView(jobId, alignment, constants, toolConfig)
      case PATSEARCH =>
        for {
          result <- PatSearchResult.patSearchResultDecoder(json, jobId)
        } yield PatSearchResultView(jobId, result, toolConfig)
    }
  }

}
