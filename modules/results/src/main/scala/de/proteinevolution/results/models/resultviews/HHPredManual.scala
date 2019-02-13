package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.ConstantsV2
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class HHPredManual(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.hhpred.forward(s"${constants.jobPath}$jobId/results/tomodel.pir", jobId),
    SUMMARY -> views.html.resultpanels.fileview(
      s"${constants.jobPath}$jobId/results/results.out",
      "HHPRED_MANUAL"
    )
  )

}
