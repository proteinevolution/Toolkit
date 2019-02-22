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

import de.proteinevolution.common.models.ToolName
import de.proteinevolution.tools.ToolConfig
import play.twirl.api.HtmlFormat

import scala.collection.immutable.ListMap

case class MarcoilResultView(jobId: String, toolConfig: ToolConfig) extends ResultView {

  override lazy val tabs: ListMap[String, HtmlFormat.Appendable] = ListMap(
    "CC-Prob" -> views.html.resultpanels.marcoil(
      s"/results/files/$jobId/alignment_ncoils.png",
      toolConfig.values(ToolName.MARCOIL.value)
    ),
    "ProbList" -> views.html.resultpanels.fileviewWithDownload(
      "alignment.ProbList",
      jobId,
      "marcoil_problist"
    ),
    "ProbState" -> views.html.resultpanels.fileviewWithDownload(
      "alignment.ProbPerState",
      jobId,
      "marcoil_probperstate"
    ),
    "Predicted Domains" -> views.html.resultpanels.fileviewWithDownload(
      "alignment.Domains",
      jobId,
      "marcoil_domains"
    )
  )

}
