package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.AlignmentResult
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class MsaProbsResultView(
    jobId: String,
    alignment: AlignmentResult,
    constants: ConstantsV2,
    toolConfig: ToolConfig
) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    CLUSTAL -> views.html.resultpanels.clustal(
      jobId,
      alignment,
      "alignment",
      toolConfig.values(ToolName.MSAPROBS.value)
    ),
    ALIGNMENT ->
    views.html.resultpanels.alignment(
      jobId,
      alignment,
      "alignment",
      toolConfig.values(ToolName.MSAPROBS.value)
    ),
    ALIGNMENTVIEWER -> views.html.resultpanels.msaviewer(s"${constants.jobPath}/$jobId/results/alignment.fas")
  )

}
