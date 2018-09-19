package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.{ Alignment, HHBlits }
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.{ JsArray, JsValue }

import scala.collection.immutable.ListMap

case class HHBlitsResultView(
    jobId: String,
    result: JsValue,
    hhblits: HHBlits,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.hhblits.hitlist(
      jobId,
      hhblits.parseResult(result),
      toolConfig.values(ToolName.HHBLITS.value),
      s"${constants.jobPath}/$jobId/results/$jobId.html_NOIMG"
    ),
    "Raw Output"   -> views.html.resultpanels.fileviewWithDownload(jobId + ".hhr", jobId, "hhblits_hhr"),
    "E-Value Plot" -> views.html.resultpanels.evalues(hhblits.parseResult(result).HSPS.map(_.info.evalue)),
    "Query Template MSA" -> views.html.resultpanels.alignmentQueryMSA(
      jobId,
      Alignment.parse((result \ "querytemplate").as[JsArray]),
      "querytemplate",
      toolConfig.values(ToolName.HHBLITS.value)
    ),
    "Query Alignment" -> views.html.resultpanels.alignmentQueryMSA(
      jobId,
      Alignment.parse((result \ "reduced").as[JsArray]),
      "reduced",
      toolConfig.values(ToolName.HHBLITS.value)
    )
  )

}
