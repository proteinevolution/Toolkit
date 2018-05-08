package exports.results.resultviews

import exports.extensions.JQueryExtensions
import exports.facades.ResultContext
import org.scalajs.jquery.{ JQuery, JQueryXHR }

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("ClustalResultView")
class ClustalResultView(container: JQuery,
                        jobID: String,
                        resultName: String,
                        var colorAAs: Boolean,
                        val tempShownHits: Int,
                        val resultContext: ResultContext)
    extends ResultView(container) {

  override def init(): Unit = {
    if (resultContext.numHits > 0) {
      bindEvents()
      showHits(0, this.shownHits)
    }
  }

  override def showHits(start: Int, end: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
    internalShowHits(
      s"/results/alignment/clustal/$jobID",
      JSON.stringify(
        js.Dictionary("color" -> colorAAs, "resultName" -> resultName)
      ),
      container.find("#resultTable"),
      start,
      end,
      successCallback
    )
  }

  def toggleAlignmentColoring(): Unit = {
    this.colorAAs = !this.colorAAs
    container.find(".colorAA").toggleClass("colorToggleBar")
    container.find("#resultTable").empty()
    showHits(0, this.shownHits)
  }

  override def bindEvents(): Unit = {
    container
      .find(".colorAA")
      .on("click", () => {
        toggleAlignmentColoring()
      })
    container
      .find(".selectAllSeqBar")
      .on(
        "click",
        () => {
          container.find(".selectAllSeqBar").toggleClass("colorToggleBar")
          JQueryExtensions.toggleText(container.find(".selectAllSeqBar"), "Select all", "Deselect all")
          checkboxes.toggleAll(resultContext.numHits)
        }
      )
  }
}
