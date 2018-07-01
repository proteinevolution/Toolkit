package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.PSIBlast
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.JsValue

case class PsiBlastResultView(
    jobId: String,
    result: JsValue,
    psi: PSIBlast,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs = Map(
    ResultViews.RESULTS -> views.html.resultpanels.psiblast.hitlist(
      jobId,
      psi.parseResult(result),
      toolConfig.values("psiblast"),
      s"${constants.jobPath}$jobId/results/blastviz.html"
    ),
    "Raw Output" -> views.html.resultpanels.fileviewWithDownload(
      "output_psiblastp.html",
      jobId,
      "PSIBLAST_OUTPUT"
    ),
    "E-Value Plot" -> views.html.resultpanels.evalues(psi.parseResult(result).HSPS.map(_.evalue))
  )

}
