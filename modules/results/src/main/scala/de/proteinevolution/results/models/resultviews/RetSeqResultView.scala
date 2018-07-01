package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.services.ToolConfig

import scala.collection.immutable.ListMap

case class RetSeqResultView(jobId: String, constants: ConstantsV2, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.SUMMARY -> views.html.resultpanels.fileview(
      s"${constants.jobPath}$jobId/results/unretrievable",
      "RETSEQ"
    ),
    ResultViews.RESULTS -> views.html.resultpanels.fileviewWithDownloadForward(
      "sequences.fa",
      jobId,
      "retseq",
      toolConfig.values(ToolName.RETSEQ.value)
    )
  )

}
