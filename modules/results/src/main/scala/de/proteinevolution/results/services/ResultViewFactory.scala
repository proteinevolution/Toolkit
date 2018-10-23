package de.proteinevolution.results.services

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.ToolName._
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.results.models.resultviews._
import de.proteinevolution.results.results._
import de.proteinevolution.services.ToolConfig
import io.circe.Json
import javax.inject.{ Inject, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class ResultViewFactory @Inject()(
    constants: ConstantsV2,
    toolConfig: ToolConfig,
    resultFiles: ResultFileAccessor
)(implicit ec: ExecutionContext) {

  import io.circe.DecodingFailure

  // TODO pass error to the frontend if the result is NONE

  def apply(toolName: String, jobId: String): OptionT[Future, ResultView] = {
    if (hasResultsJson(toolName)) {
      for {
        result <- OptionT(resultFiles.getResults(jobId))
        view   <- OptionT.fromOption[Future](getResultViewsWithJson(toolName, jobId, result).toOption)
      } yield {
        view
      }
    } else {
      OptionT.pure[Future](getResultViewsWithoutJson(toolName, jobId))
    }
  }

  private val toolsWithResultJson: List[String] =
    (PSIBLAST :: TPRPRED :: HHBLITS :: HMMER :: HHPRED :: HHOMP ::
    HHPRED_ALIGN :: QUICK2D :: CLUSTALO :: KALIGN :: MAFFT :: MSAPROBS ::
    MUSCLE :: TCOFFEE :: SEQ2ID :: HHFILTER :: PATSEARCH :: Nil).map(_.value)

  private def hasResultsJson(toolName: String): Boolean = {
    toolsWithResultJson.contains(toolName)
  }

  private def getResultViewsWithoutJson(toolName: String, jobId: String): ResultView = {
    toolName match {
      case FORMATSEQ.value           => FormatSeqResultView(jobId, toolConfig)
      case CLANS.value               => ClansResultView(jobId)
      case MARCOIL.value             => MarcoilResultView(jobId, toolConfig)
      case DEEPCOIL.value            => DeepCoilResultView(jobId, toolConfig, constants)
      case PCOILS.value              => PcoilsResultView(jobId, constants)
      case REPPER.value              => RepperResultView(jobId, constants)
      case MODELLER.value            => ModellerResultView(jobId, constants)
      case HHPRED_MANUAL.value       => HHPredManual(jobId, constants)
      case HHREPID.value             => HHrepIdResultView(jobId, constants)
      case ALI2D.value               => Ali2DResultView(jobId, constants)
      case ALN2PLOT.value            => Aln2PlotResultView(jobId)
      case ANCESCON.value            => AncesconResultView(jobId, constants)
      case PHYML.value               => PhyMLResultView(jobId, constants)
      case MMSEQS2.value             => MMSeqsResultView(jobId, toolConfig)
      case RETSEQ.value              => RetSeqResultView(jobId, constants, toolConfig)
      case SAMCC.value               => SamCCResultView(jobId)
      case SIXFRAMETRANSLATION.value => SixFrameTranslationResultView(jobId)
      case BACKTRANS.value           => BackTransResultView(jobId)
    }
  }

  private def getResultViewsWithJson(
      toolName: String,
      jobId: String,
      json: Json
  ): Either[DecodingFailure, ResultView] = {
    toolName match {
      case PSIBLAST.value =>
        for {
          result <- json.as[PSIBlastResult]
        } yield {
          PsiBlastResultView(jobId, result, toolConfig, constants)
        }
      case TPRPRED.value =>
        for {
          result <- TPRPredResult.tprpredDecoder(jobId, json)
        } yield {
          TprPredResultView(jobId, result)
        }
      case HHBLITS.value =>
        for {
          result    <- json.as[HHBlitsResult]
          alignment <- json.hcursor.downField("querytemplate").as[AlignmentResult]
          reduced   <- json.hcursor.downField("reduced").as[AlignmentResult]
        } yield {
          HHBlitsResultView(jobId, result, alignment, reduced, toolConfig, constants)
        }
      case HMMER.value =>
        for {
          result <- json.as[HmmerResult]
        } yield {
          HmmerResultView(jobId, result, toolConfig, constants)
        }
      case HHPRED.value =>
        for {
          result    <- json.as[HHPredResult]
          alignment <- json.hcursor.downField("querytemplate").as[AlignmentResult]
          reduced   <- json.hcursor.downField("reduced").as[AlignmentResult]
        } yield {
          HHPredResultView(jobId, result, alignment, reduced, toolConfig, constants)
        }
      case HHOMP.value =>
        for {
          result <- json.as[HHompResult]
        } yield {
          HHompResultView(jobId, result, constants, toolConfig)
        }
      case HHPRED_ALIGN.value =>
        for {
          result <- json.as[HHPredResult]
        } yield {
          HHPredAlignResultView(jobId, result, toolConfig, constants)
        }
      case QUICK2D.value =>
        for {
          result <- json.as[Quick2DResult]
        } yield {
          Quick2DResultView(result)
        }
      case CLUSTALO.value =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield {
          ClustalOmegaResultView(jobId, alignment, constants, toolConfig)
        }
      case KALIGN.value =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield {
          KalignResultView(jobId, alignment, constants, toolConfig)
        }
      case MAFFT.value =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield {
          MafftResultView(jobId, alignment, constants, toolConfig)
        }
      case MSAPROBS.value =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield {
          MsaProbsResultView(jobId, alignment, constants, toolConfig)
        }
      case MUSCLE.value =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield {
          MuscleResultView(jobId, alignment, constants, toolConfig)
        }
      case TCOFFEE.value =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield {
          TcoffeeResultView(jobId, alignment, constants, toolConfig)
        }
      case SEQ2ID.value =>
        for {
          result <- json.as[Unchecked]
        } yield {
          Seq2IdResultView(jobId, result)
        }
      case HHFILTER.value =>
        for {
          alignment <- json.hcursor.downField("alignment").as[AlignmentResult]
        } yield {
          HHFilterResultView(jobId, alignment, constants, toolConfig)
        }
      case PATSEARCH.value =>
        for {
          result <- PatSearchResult.patSearchResultDecoder(json, jobId)
        } yield {
          PatSearchResultView(jobId, result, toolConfig)
        }
    }
  }

}
