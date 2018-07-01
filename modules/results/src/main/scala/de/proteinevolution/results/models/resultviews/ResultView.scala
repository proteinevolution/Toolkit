package de.proteinevolution.results.models.resultviews

import play.twirl.api.HtmlFormat

trait ResultView {

  def tabs: Map[String, HtmlFormat.Appendable]

}
