/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.results.models.resultviews

import de.proteinevolution.common.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results.{ AlignmentResult, HHBlitsResult }
import de.proteinevolution.tools.ToolConfig

import scala.collection.immutable.ListMap

case class HHBlitsResultView(
    jobId: String,
    result: HHBlitsResult,
    alignment: AlignmentResult,
    reduced: AlignmentResult,
    toolConfig: ToolConfig,
    constants: ConstantsV2
) extends ResultView {
  import play.twirl.api.HtmlFormat

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    RESULTS -> views.html.resultpanels.hhblits.hitlist(
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
