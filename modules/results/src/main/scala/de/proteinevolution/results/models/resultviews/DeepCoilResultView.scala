package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.services.ToolConfig

import scala.collection.immutable.ListMap

case class DeepCoilResultView(jobId: String, toolConfig: ToolConfig, constants: ConstantsV2) extends ResultView {

  override lazy val tabs = ListMap(
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
