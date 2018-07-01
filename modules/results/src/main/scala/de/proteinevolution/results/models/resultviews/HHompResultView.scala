package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.HHomp
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.JsValue

case class HHompResultView(
    jobId: String,
    result: JsValue,
    constants: ConstantsV2,
    hhomp: HHomp,
    toolConfig: ToolConfig
) extends ResultView {

  override lazy val tabs = Map(
    ResultViews.RESULTS ->
    views.html.resultpanels.hhomp.hitlist(
      jobId,
      hhomp.parseResult(result),
      toolConfig.values(ToolName.HHOMP.value),
      s"${constants.jobPath}/$jobId/results/$jobId.html_NOIMG"
    ),
    "Raw Output" ->
    views.html.resultpanels.fileviewWithDownload(jobId + ".hhr", jobId, "hhomp")
  )

}
