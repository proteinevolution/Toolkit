package exports.results.resultviews

import exports.extensions.JQueryExtensions
import exports.facades.ResultContext
import org.scalajs.dom
import org.scalajs.jquery.{ jQuery, JQuery, JQueryAjaxSettings, JQueryXHR }

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("ClustalResultView")
class ClustalResultView(container: JQuery,
                        jobID: String,
                        resultName: String,
                        var colorAAs: Boolean,
                        tempShownHits: Int,
                        resultContext: ResultContext)
    extends ResultView(container, jobID, tempShownHits, resultContext) {

  override def init(): Unit = {
    if (resultContext.numHits > 0) {
      bindEvents()
      showHits(0, this.shownHits)
    }
  }

  override def showHits(start: Int, end: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
    loading = true
    container.find("#loadingHits").show()
    container.find("#loadHits").hide()
    val route = s"/results/alignment/clustal/$jobID"
    jQuery.ajax(
      js.Dynamic
        .literal(
          url = route,
          data = JSON.stringify(
            js.Dynamic.literal("color" -> colorAAs, "resultName" -> resultName)
          ),
          contentType = "application/json",
          success = { (data: js.Any, _: js.Any, _: JQueryXHR) =>
            container.find("#resultTable").append(data.asInstanceOf[String])
            shownHits = end
            if (shownHits != resultContext.numHits)
              container.find("#loadHits").show()
            checkboxes.initForContainer(container.find("#resultTable"))
            container.find("#loadingHits").hide()
            loading = false
          },
          error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
            println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
            container.find("#resultTable").append("Error loading Data.")
            container.find("#loadingHits").hide()
            loading = false
          },
          `type` = "POST"
        )
        .asInstanceOf[JQueryAjaxSettings]
    )
  }

  def toggleAlignmentColoring(): Unit = {
    this.colorAAs = !this.colorAAs
    js.Dynamic.global.$.LoadingOverlay("show")
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
