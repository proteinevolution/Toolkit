package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.Hmmer
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.JsValue

import scala.collection.immutable.ListMap

case class HmmerResultView(
    jobId: String,
    result: JsValue,
    hmmer: Hmmer,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.hmmer.hitlist(
      jobId,
      hmmer.parseResult(result),
      toolConfig.values(ToolName.HMMER.value),
      s"${constants.jobPath}/$jobId/results/blastviz.html"
    ),
    "E-Value Plot" -> views.html.resultpanels
      .evalues(hmmer.parseResult(result).HSPS.map(_.evalue))
  )

}
