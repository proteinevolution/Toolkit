package de.proteinevolution.results.models.resultviews

import de.proteinevolution.results.results.Quick2DResult
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class Quick2DResultView(result: Quick2DResult) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.quick2d(result)
  )

}
