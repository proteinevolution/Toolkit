package de.proteinevolution.results.services

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.models.ToolName._
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.results.models.resultviews._
import de.proteinevolution.results.results._
import de.proteinevolution.tools.ToolConfig
import io.circe.{ DecodingFailure, Json }
import javax.inject.{ Inject, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class ResultViewFactory @Inject()(
    constants: ConstantsV2,
    toolConfig: ToolConfig,
    resultFiles: ResultFileAccessor
)(implicit ec: ExecutionContext) {

  def apply(toolName: String, jobId: String): OptionT[Future, ResultView] = {
    if (ToolName(toolName).hasJson) {
      for {
        result <- OptionT(resultFiles.getResults(jobId))
        view   <- OptionT.fromOption[Future](getResultViewsWithJson(toolName, jobId, result).toOption)
      } yield view
    } else {
      OptionT.pure[Future](getResultViewsWithoutJson(toolName, jobId))
    }
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
      case ANCESCON            => AncesconResultView(jobId, constants)
      case PHYML               => PhyMLResultView(jobId, constants)
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
      case QUICK2D =>
        for {
          result <- json.as[Quick2DResult]
        } yield Quick2DResultView(result)
      case CLUSTALO =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield ClustalOmegaResultView(jobId, alignment, constants, toolConfig)
      case KALIGN =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield KalignResultView(jobId, alignment, constants, toolConfig)
      case MAFFT =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield MafftResultView(jobId, alignment, constants, toolConfig)
      case MSAPROBS =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield MsaProbsResultView(jobId, alignment, constants, toolConfig)
      case MUSCLE =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield MuscleResultView(jobId, alignment, constants, toolConfig)
      case TCOFFEE =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield TcoffeeResultView(jobId, alignment, constants, toolConfig)
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
