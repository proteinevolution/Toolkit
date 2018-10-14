package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.Quick2DResult

import scala.collection.immutable.ListMap

case class Quick2DResultView(result: Quick2DResult) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.quick2d(result)
  )

}
