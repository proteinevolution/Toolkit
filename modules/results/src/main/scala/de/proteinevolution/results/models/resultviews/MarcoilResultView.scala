package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.ToolName
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class MarcoilResultView(jobId: String, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    "CC-Prob" -> views.html.resultpanels.marcoil(
      s"/results/files/$jobId/alignment_ncoils.png",
      toolConfig.values(ToolName.MARCOIL.value)
    ),
    "ProbList" -> views.html.resultpanels.fileviewWithDownload(
      "alignment.ProbList",
      jobId,
      "marcoil_problist"
    ),
    "ProbState" -> views.html.resultpanels.fileviewWithDownload(
      "alignment.ProbPerState",
      jobId,
      "marcoil_probperstate"
    ),
    "Predicted Domains" -> views.html.resultpanels.fileviewWithDownload(
      "alignment.Domains",
      jobId,
      "marcoil_domains"
    )
  )

}
