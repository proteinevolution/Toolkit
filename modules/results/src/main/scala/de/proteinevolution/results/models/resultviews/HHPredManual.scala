package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.results.ResultViews

import scala.collection.immutable.ListMap

case class HHPredManual(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.hhpred
      .forward(s"${constants.jobPath}$jobId/results/tomodel.pir", jobId),
    ResultViews.SUMMARY -> views.html.resultpanels.fileview(
      s"${constants.jobPath}$jobId/results/results.out",
      "HHPRED_MANUAL"
    )
  )

}
