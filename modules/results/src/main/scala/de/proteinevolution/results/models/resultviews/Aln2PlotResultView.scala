package de.proteinevolution.results.models.resultviews

case class Aln2PlotResultView(jobId: String) extends ResultView {

  override lazy val tabs = Map("Plots" -> views.html.resultpanels.aln2plot(jobId))

}
