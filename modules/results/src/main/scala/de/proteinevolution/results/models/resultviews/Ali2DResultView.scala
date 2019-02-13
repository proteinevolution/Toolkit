package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.ConstantsV2
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class Ali2DResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.fileview(s"${constants.jobPath}$jobId/results/" + jobId + ".results_color",
                                                "ALI2D_COLOR"),
    "Results With Confidence" -> views.html.resultpanels
      .fileview(s"${constants.jobPath}$jobId/results/" + jobId + ".results_colorC", "ALI2D_COLOR_CONF"),
    "Text output" -> views.html.resultpanels.fileview(
      s"${constants.jobPath}$jobId/results/" + jobId + ".results",
      "ALI2D_TEXT"
    )
  )

}
