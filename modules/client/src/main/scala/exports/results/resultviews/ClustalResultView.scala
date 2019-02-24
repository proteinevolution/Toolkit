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

package exports.results.resultviews

import exports.facades.ResultContext
import exports.results.models.ResultForm.ClustalResultForm
import org.scalajs.jquery.{ jQuery, JQuery, JQueryXHR }
import exports.facades.JQueryPlugin._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("ClustalResultView")
class ClustalResultView(
    val container: JQuery,
    val jobID: String,
    resultName: String,
    var colorAAs: Boolean,
    val hitsToLoad: Int,
    val resultContext: ResultContext
) extends ResultView {

  override def init(): Unit = {
    if (resultContext.numHits > 0) {
      bindEvents()
      showHits(0, hitsToLoad)
    }
  }

  override def showHits(start: Int, end: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
    internalShowHits(
      jobID,
      s"/results/alignment/clustal/$jobID",
      ClustalResultForm(start, end, colorAAs, resultName),
      container.find("#resultTable"),
      successCallback
    )
  }

  def toggleAlignmentColoring(): Unit = {
    colorAAs = !colorAAs
    container.find(".colorAA").toggleClass("colorToggleBar")
    container.find("#resultTable").empty()
    jQuery.LoadingOverlay("show")
    showHits(0, shownHits)
  }

  def bindEvents(): Unit = {
    commonBindEvents()
    container
      .find(".colorAA")
      .off("click")
      .on("click", () => {
        toggleAlignmentColoring()
      })
  }
}
