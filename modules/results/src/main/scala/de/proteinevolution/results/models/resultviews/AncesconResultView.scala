package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.ConstantsV2
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class AncesconResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    TREE ->
    views.html.resultpanels.tree(
      jobId + ".clu.tre",
      s"${constants.jobPath}$jobId/results/" + jobId + ".clu.tre",
      jobId,
      "ANCESCON"
    ),
    DATA -> views.html.resultpanels.fileviewWithDownload(
      jobId + ".anc_out",
      jobId,
      "ancescon_output_data"
    )
  )

}
