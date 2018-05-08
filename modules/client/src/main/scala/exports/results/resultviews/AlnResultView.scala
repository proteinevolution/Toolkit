package exports.results.resultviews

import exports.extensions.JQueryExtensions
import exports.facades.ResultContext
import org.scalajs.dom
import org.scalajs.jquery.{ jQuery, JQuery, JQueryXHR }

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("AlnResultView")
class AlnResultView(container: JQuery,
                    jobID: String,
                    resultName: String,
                    val tempShownHits: Int,
                    val resultContext: ResultContext)
    extends ResultView(container) {

  override def init(): Unit = {
    scrollUtil.followScroll(jQuery(dom.document))
    if (resultContext.numHits > 0) {
      bindEvents()
      showHits(0, this.shownHits)
    }
  }

  override def showHits(start: Int, end: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
    internalShowHits(
      s"/results/alignment/loadHits/$jobID",
      JSON.stringify(
        js.Dictionary("start" -> start, "end" -> end, "resultName" -> resultName)
      ),
      container.find(".alignmentTBody"),
      start,
      end,
      successCallback
    )
  }

  override def bindEvents(): Unit = {
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
