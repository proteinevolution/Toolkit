package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ToolName
import de.proteinevolution.results.results.PatSearchResult
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class PatSearchResultView(jobId: String, result: PatSearchResult, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.patternSearch(
      jobId,
      result,
      toolConfig.values(ToolName.PATSEARCH.value)
    )
  )

}
