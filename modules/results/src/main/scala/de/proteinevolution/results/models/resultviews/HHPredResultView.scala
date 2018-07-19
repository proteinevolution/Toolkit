package de.proteinevolution.results.models.resultviews

import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.{ Alignment, HHPred }
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.{ JsArray, JsValue }

import scala.collection.immutable.ListMap

case class HHPredResultView(
    jobId: String,
    result: JsValue,
    hhpred: HHPred,
    toolConfig: ToolConfig,
    aln: Alignment,
    constants: ConstantsV2
) extends ResultView {

  override lazy val tabs = ListMap(
    ResultViews.RESULTS -> views.html.resultpanels.hhpred.hitlist(
      jobId,
      hhpred.parseResult(result),
      toolConfig.values(ToolName.HHPRED.value),
      s"${constants.jobPath}/$jobId/results/$jobId.html_NOIMG"
    ),
    "Raw Output" -> views.html.resultpanels.fileviewWithDownload(jobId + ".hhr",
                                                                 jobId,
                                                                 "hhpred"),
    "Probability  Plot" -> views.html.resultpanels
      .probability(hhpred.parseResult(result).HSPS.map(_.info.probab)),
    "Query Template MSA" -> views.html.resultpanels.alignment(
      jobId,
      aln.parse((result \ "querytemplate").as[JsArray]),
      "querytemplate",
      toolConfig.values(ToolName.HHPRED.value)
    ),
    "Query MSA" -> views.html.resultpanels.alignmentQueryMSA(
      jobId,
      aln.parse((result \ "reduced").as[JsArray]),
      "reduced",
      toolConfig.values(ToolName.HHPRED.value)
    )
  )

}
