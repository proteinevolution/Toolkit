package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.{ ConstantsV2, ToolName }
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class DeepCoilResultView(
    jobId: String,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    "CC-Prob" -> views.html.resultpanels.marcoil(
      s"/results/files/$jobId/" + jobId + "_deepcoil.png",
      toolConfig.values(ToolName.DEEPCOIL.value)
    ),
    "ProbList" -> views.html.resultpanels.fileview(
      s"${constants.jobPath}$jobId/results/" + jobId + "_deepcoil",
      "PCOILS_PROBLIST"
    )
  )

}
