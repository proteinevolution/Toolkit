package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.HHompResult
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class HHompResultView(
    jobId: String,
    result: HHompResult,
    constants: ConstantsV2,
    toolConfig: ToolConfig
) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS ->
    views.html.resultpanels.hhomp.hitlist(
      jobId,
      result,
      toolConfig.values(ToolName.HHOMP.value),
      s"${constants.jobPath}/$jobId/results/$jobId.html_NOIMG"
    ),
    "Raw Output" ->
    views.html.resultpanels.fileviewWithDownload(jobId + ".hhr", jobId, "hhomp")
  )

}
