package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2

import scala.collection.immutable.ListMap

case class PcoilsResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs = ListMap(
    "CC-Prob" -> views.html.resultpanels.pcoils(s"/results/files/$jobId/" + jobId),
    "ProbList" -> views.html.resultpanels.fileview(
      s"${constants.jobPath}$jobId/results/" + jobId + ".numerical",
      "PCOILS_PROBLIST"
    )
  )

}
