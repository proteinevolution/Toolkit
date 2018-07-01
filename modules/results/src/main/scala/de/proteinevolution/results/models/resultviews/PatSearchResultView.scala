package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ToolName
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.JsValue

case class PatSearchResultView(jobId: String, result: JsValue, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs = Map(
    ResultViews.RESULTS -> views.html.resultpanels.patternSearch(
      jobId,
      result,
      toolConfig.values(ToolName.PATSEARCH.value)
    )
  )

}
