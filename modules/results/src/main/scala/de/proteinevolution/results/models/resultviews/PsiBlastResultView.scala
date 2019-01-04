package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.results.results.PSIBlastResult
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class PsiBlastResultView(
    jobId: String,
    result: PSIBlastResult,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.psiblast.hitlist(
      jobId,
      result,
      toolConfig.values("psiblast"),
      s"${constants.jobPath}$jobId/results/blastviz.html"
    ),
    "Raw Output" -> views.html.resultpanels.fileviewWithDownload(
      "output_psiblastp.html",
      jobId,
      "PSIBLAST_OUTPUT"
    ),
    "E-Value Plot" -> views.html.resultpanels.evalues(result.HSPS.map(_.eValue))
  )

}
