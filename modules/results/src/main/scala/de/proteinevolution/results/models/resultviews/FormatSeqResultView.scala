package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ToolName
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.services.ToolConfig

import scala.collection.immutable.ListMap

case class FormatSeqResultView(jobId: String, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.fileviewWithDownloadForward(
      jobId + ".out",
      jobId,
      "FormatSeq",
      toolConfig.values(ToolName.FORMATSEQ.value)
    )
  )

}
