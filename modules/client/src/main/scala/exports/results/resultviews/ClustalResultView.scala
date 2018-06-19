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
