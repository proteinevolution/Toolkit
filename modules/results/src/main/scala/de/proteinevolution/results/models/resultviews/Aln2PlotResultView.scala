package de.proteinevolution.results.models.resultviews

import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class Aln2PlotResultView(jobId: String) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    "Plots" -> views.html.resultpanels.aln2plot(jobId)
  )

}
