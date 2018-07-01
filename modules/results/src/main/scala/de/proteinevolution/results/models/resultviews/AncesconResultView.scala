package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.results.ResultViews

case class AncesconResultView(jobId: String, constants: ConstantsV2) extends ResultView {

  override lazy val tabs = Map(
    ResultViews.TREE ->
    views.html.resultpanels.tree(
      jobId + ".clu.tre",
      s"${constants.jobPath}$jobId/results/" + jobId + ".clu.tre",
      jobId,
      "ANCESCON"
    ),
    ResultViews.DATA -> views.html.resultpanels.fileviewWithDownload(
      jobId + ".anc_out",
      jobId,
      "ancescon_output_data"
    )
  )

}
