package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews

import scala.collection.immutable.ListMap

case class BackTransResultView(jobId: String) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels
      .fileviewWithDownload(jobId + ".out", jobId, "backtrans")
  )

}
