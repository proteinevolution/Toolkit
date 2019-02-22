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

import de.proteinevolution.common.models.ConstantsV2
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
