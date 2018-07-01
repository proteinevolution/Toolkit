package de.proteinevolution.results.models.resultviews

import play.twirl.api.HtmlFormat
import scala.collection.immutable.ListMap

trait ResultView {

  def tabs: ListMap[String, HtmlFormat.Appendable]

}
