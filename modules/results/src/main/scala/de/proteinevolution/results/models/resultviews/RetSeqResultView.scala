package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class RetSeqResultView(jobId: String, constants: ConstantsV2, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    SUMMARY -> views.html.resultpanels.fileview(
      s"${constants.jobPath}$jobId/results/unretrievable",
      "RETSEQ"
    ),
    RESULTS -> views.html.resultpanels.fileviewWithDownloadForward(
      "sequences.fa",
      jobId,
      "retseq",
      toolConfig.values(ToolName.RETSEQ.value)
    )
  )

}
