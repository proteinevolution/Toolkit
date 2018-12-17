package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.{ AlignmentResult, HHPredResult }
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class HHPredResultView(
    jobId: String,
    result: HHPredResult,
    alignment: AlignmentResult,
    reduced: AlignmentResult,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.hhpred.hitlist(
      jobId,
      result,
      toolConfig.values(ToolName.HHPRED.value),
      s"${constants.jobPath}/$jobId/results/$jobId.html_NOIMG"
    ),
    "Raw Output"        -> views.html.resultpanels.fileviewWithDownload(jobId + ".hhr", jobId, "hhpred"),
    "Probability  Plot" -> views.html.resultpanels.probability(result.HSPS.map(_.info.probab)),
    "Query Template MSA" -> views.html.resultpanels.alignment(
      jobId,
      alignment,
      "querytemplate",
      toolConfig.values(ToolName.HHPRED.value)
    ),
    "Query MSA" -> views.html.resultpanels.alignmentQueryMSA(
      jobId,
      reduced,
      "reduced",
      toolConfig.values(ToolName.HHPRED.value)
    )
  )

}
