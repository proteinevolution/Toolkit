package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.results.ResultViews

import scala.collection.immutable.ListMap

case class RepperResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.repper(jobId, s"${constants.jobPath}$jobId/results/" + jobId)
  )

}
