package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.results.ResultViews

case class SixFrameTranslationResultView(jobId: String) extends ResultView {

  override lazy val tabs = Map(
    ResultViews.RESULTS -> views.html.resultpanels.fileviewWithDownload(
      jobId + ".out",
      jobId,
      "sixframetrans"
    )
  )

}
