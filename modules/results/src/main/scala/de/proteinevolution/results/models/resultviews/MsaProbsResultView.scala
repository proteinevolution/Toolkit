package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.Alignment
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.{ JsArray, JsValue }

case class MsaProbsResultView(
    jobId: String,
    result: JsValue,
    constants: ConstantsV2,
    toolConfig: ToolConfig,
    aln: Alignment
) extends ResultView {

  override lazy val tabs = Map(
    ResultViews.CLUSTAL -> views.html.resultpanels.clustal(
      jobId,
      aln.parse((result \ "alignment").as[JsArray]),
      "alignment",
      toolConfig.values(ToolName.MSAPROBS.value)
    ),
    ResultViews.ALIGNMENT ->
    views.html.resultpanels.alignment(
      jobId,
      aln.parse((result \ "alignment").as[JsArray]),
      "alignment",
      toolConfig.values(ToolName.MSAPROBS.value)
    ),
    ResultViews.ALIGNMENTVIEWER -> views.html.resultpanels
      .msaviewer(s"${constants.jobPath}/$jobId/results/alignment.fas")
  )

}
