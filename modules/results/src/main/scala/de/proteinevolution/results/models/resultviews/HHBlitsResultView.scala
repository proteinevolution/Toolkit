package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.{ AlignmentResult, HHBlitsResult }
import de.proteinevolution.services.ToolConfig

import scala.collection.immutable.ListMap

case class HHBlitsResultView(
    jobId: String,
    result: HHBlitsResult,
    alignment: AlignmentResult,
    reduced: AlignmentResult,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.hhblits.hitlist(
      jobId,
      result,
      toolConfig.values(ToolName.HHBLITS.value),
      s"${constants.jobPath}/$jobId/results/$jobId.html_NOIMG"
    ),
    "Raw Output"   -> views.html.resultpanels.fileviewWithDownload(jobId + ".hhr", jobId, "hhblits_hhr"),
    "E-Value Plot" -> views.html.resultpanels.evalues(result.HSPS.map(_.info.eval)),
    "Query Template MSA" -> views.html.resultpanels.alignmentQueryMSA(
      jobId,
      alignment,
      "querytemplate",
      toolConfig.values(ToolName.HHBLITS.value)
    ),
    "Query Alignment" -> views.html.resultpanels.alignmentQueryMSA(
      jobId,
      reduced,
      "reduced",
      toolConfig.values(ToolName.HHBLITS.value)
    )
  )

}
