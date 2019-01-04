package de.proteinevolution.results.models.resultviews

import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class BackTransResultView(jobId: String) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.fileviewWithDownload(jobId + ".out", jobId, "backtrans")
  )

}
