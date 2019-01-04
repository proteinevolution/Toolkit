package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class RepperResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.repper(jobId, s"${constants.jobPath}$jobId/results/" + jobId)
  )

}
