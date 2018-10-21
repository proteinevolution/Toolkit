package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.AlignmentResult
import de.proteinevolution.services.ToolConfig

import scala.collection.immutable.ListMap

case class HHFilterResultView(
    jobId: String,
    alignment: AlignmentResult,
    constants: ConstantsV2,
    toolConfig: ToolConfig
) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.ALIGNMENT -> views.html.resultpanels.alignment(
      jobId,
      alignment,
      "alignment",
      toolConfig.values(ToolName.HHFILTER.value)
    ),
    ResultViews.ALIGNMENTVIEWER -> views.html.resultpanels
      .msaviewer(s"${constants.jobPath}/$jobId/results/alignment.fas")
  )

}
