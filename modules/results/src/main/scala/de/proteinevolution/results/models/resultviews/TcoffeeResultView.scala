package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.Alignment
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.{ JsArray, JsValue }

import scala.collection.immutable.ListMap

case class TcoffeeResultView(
    jobId: String,
    result: JsValue,
    constants: ConstantsV2,
    toolConfig: ToolConfig
) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.CLUSTAL ->
    views.html.resultpanels.clustal(
      jobId,
      Alignment.parse((result \ "alignment").as[JsArray]),
      "alignment",
      toolConfig.values(ToolName.TCOFFEE.value)
    ),
    ResultViews.ALIGNMENT ->
    views.html.resultpanels.alignment(
      jobId,
      Alignment.parse((result \ "alignment").as[JsArray]),
      "alignment",
      toolConfig.values(ToolName.TCOFFEE.value)
    ),
    ResultViews.ALIGNMENTVIEWER -> views.html.resultpanels
      .msaviewer(s"${constants.jobPath}/$jobId/results/alignment.fas")
  )

}
