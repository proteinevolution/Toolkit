package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.ConstantsV2
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class PcoilsResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    "CC-Prob" -> views.html.resultpanels.pcoils(s"/results/files/$jobId/" + jobId),
    "ProbList" -> views.html.resultpanels.fileview(
      s"${constants.jobPath}$jobId/results/" + jobId + ".numerical",
      "PCOILS_PROBLIST"
    )
  )

}
