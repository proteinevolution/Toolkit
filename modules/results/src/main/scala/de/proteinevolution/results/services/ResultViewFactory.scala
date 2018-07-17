package de.proteinevolution.results.services

import cats.data.OptionT
import cats.implicits._
import javax.inject.{ Inject, Singleton }
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.ToolName._
import de.proteinevolution.results.results._
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.results.models.resultviews._
import de.proteinevolution.results.results.{ Alignment, HHBlits, HHPred, HHomp }
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.JsValue

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class ResultViewFactory @Inject()(
    psi: PSIBlast,
    hmmer: Hmmer,
    hhpred: HHPred,
    hhblits: HHBlits,
    hhomp: HHomp,
    quick2d: Quick2D,
    aln: Alignment,
    constants: ConstantsV2,
    toolConfig: ToolConfig,
    resultFiles: ResultFileAccessor
)(implicit ec: ExecutionContext) {

  def apply(toolName: String, jobId: String): OptionT[Future, ResultView] = {
    if (hasResultsJson(toolName)) {
      for {
        result <- OptionT(resultFiles.getResults(jobId))
      } yield {
        getResultViewsWithJson(toolName, jobId, result)
      }
    } else {
      OptionT.pure[Future](getResultViewsWithoutJson(toolName, jobId))
    }
  }

  private val toolsWithResultJson: List[String] =
  PSIBLAST.value :: TPRPRED.value :: HHBLITS.value :: DEEPCOIL.value :: HMMER.value :: HHPRED.value :: HHOMP.value ::
  HHPRED_ALIGN.value :: QUICK2D.value :: CLUSTALO.value :: KALIGN.value :: MAFFT.value :: MSAPROBS.value ::
  MUSCLE.value :: TCOFFEE.value :: SEQ2ID.value :: HHFILTER.value :: PATSEARCH.value :: Nil

  private def hasResultsJson(toolName: String): Boolean = {
    toolsWithResultJson.contains(toolName)
  }

  private def getResultViewsWithoutJson(toolName: String, jobId: String): ResultView = {
    toolName match {
      case FORMATSEQ.value           => FormatSeqResultView(jobId, toolConfig)
      case CLANS.value               => ClansResultView(jobId)
      case MARCOIL.value             => MarcoilResultView(jobId, toolConfig)
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

  private def getResultViewsWithJson(toolName: String, jobId: String, result: JsValue): ResultView = {
    toolName match {
      case PSIBLAST.value     => PsiBlastResultView(jobId, result, psi, toolConfig, constants)
      case TPRPRED.value      => TprPredResultView(jobId, result)
      case HHBLITS.value      => HHBlitsResultView(jobId, result, hhblits, aln, toolConfig, constants)
      case DEEPCOIL.value     => DeepCoilResultView(jobId, result, toolConfig, constants)
      case HMMER.value        => HmmerResultView(jobId, result, hmmer, toolConfig, constants)
      case HHPRED.value       => HHPredResultView(jobId, result, hhpred, toolConfig, aln, constants)
      case HHOMP.value        => HHompResultView(jobId, result, constants, hhomp, toolConfig)
      case HHPRED_ALIGN.value => HHPredAlignResultView(jobId, result, hhpred, toolConfig, constants)
      case QUICK2D.value      => Quick2DResultView(result, quick2d)
      case CLUSTALO.value     => ClustalOmegaResultView(jobId, result, constants, toolConfig, aln)
      case KALIGN.value       => KalignResultView(jobId, result, constants, toolConfig, aln)
      case MAFFT.value        => MafftResultView(jobId, result, constants, toolConfig, aln)
      case MSAPROBS.value     => MsaProbsResultView(jobId, result, constants, toolConfig, aln)
      case MUSCLE.value       => MuscleResultView(jobId, result, constants, toolConfig, aln)
      case TCOFFEE.value      => TcoffeeResultView(jobId, result, constants, toolConfig, aln)
      case SEQ2ID.value       => Seq2IdResultView(jobId, result)
      case HHFILTER.value     => HHFilterResultView(jobId, result, constants, toolConfig, aln)
      case PATSEARCH.value    => PatSearchResultView(jobId, result, toolConfig)
    }
  }

}
