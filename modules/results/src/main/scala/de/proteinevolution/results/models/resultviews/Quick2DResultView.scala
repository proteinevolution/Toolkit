package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.Quick2D
import play.api.libs.json.JsValue

import scala.collection.immutable.ListMap

case class Quick2DResultView(result: JsValue, quick2d: Quick2D) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.quick2d(quick2d.parseResult(result))
  )

}
