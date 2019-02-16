package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.ToolName
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class FormatSeqResultView(jobId: String, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.fileviewWithDownloadForward(
      jobId + ".out",
      jobId,
      "FormatSeq",
      toolConfig.values(ToolName.FORMATSEQ.value)
    )
  )

}
