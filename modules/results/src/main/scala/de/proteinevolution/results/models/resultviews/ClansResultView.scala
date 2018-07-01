package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews

case class ClansResultView(jobId: String) extends ResultView {

  override lazy val tabs = Map(ResultViews.RESULTS -> views.html.resultpanels.clans(jobId))

}
