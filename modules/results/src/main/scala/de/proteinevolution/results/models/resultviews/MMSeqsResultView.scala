package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ToolName
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class MMSeqsResultView(jobId: String, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    "Reduced set" ->
    views.html.resultpanels.fileviewWithDownloadForward(
      jobId + ".fas",
      jobId,
      "mmseqs_reps",
      toolConfig.values(ToolName.MMSEQS2.value)
    ),
    "Clusters" -> views.html.resultpanels.fileviewWithDownload(jobId + ".clu", jobId, "mmseqs_clusters")
  )

}
