package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.HmmerResult
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class HmmerResultView(
    jobId: String,
    result: HmmerResult,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.hmmer.hitlist(
      jobId,
      result,
      toolConfig.values(ToolName.HMMER.value),
      s"${constants.jobPath}/$jobId/results/blastviz.html"
    ),
    "E-Value Plot" -> views.html.resultpanels.evalues(result.HSPS.map(_.eValue))
  )

}
