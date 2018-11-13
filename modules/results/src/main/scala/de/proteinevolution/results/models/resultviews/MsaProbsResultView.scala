package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.AlignmentResult
import de.proteinevolution.tools.ToolConfig

import scala.collection.immutable.ListMap

case class MsaProbsResultView(
    jobId: String,
    alignment: AlignmentResult,
    constants: ConstantsV2,
    toolConfig: ToolConfig
) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.CLUSTAL -> views.html.resultpanels.clustal(
      jobId,
      alignment,
      "alignment",
      toolConfig.values(ToolName.MSAPROBS.value)
    ),
    ResultViews.ALIGNMENT ->
    views.html.resultpanels.alignment(
      jobId,
      alignment,
      "alignment",
      toolConfig.values(ToolName.MSAPROBS.value)
    ),
    ResultViews.ALIGNMENTVIEWER -> views.html.resultpanels
      .msaviewer(s"${constants.jobPath}/$jobId/results/alignment.fas")
  )

}
