package exports.results.resultviews

import exports.extensions.JQueryExtensions
import exports.facades.ResultContext
import org.scalajs.jquery.{ jQuery, JQuery, JQueryAjaxSettings, JQueryXHR }

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("AlnResultView")
class AlnResultView(container: JQuery,
                    jobID: String,
                    resultName: String,
                    val tempShownHits: Int,
                    val resultContext: ResultContext)
    extends ResultView(container, jobID) {

  override def init(): Unit = {

    if (resultContext.numHits > 0) {
      bindEvents()
      showHits(0, this.shownHits)
    }
  }

  override def showHits(start: Int, end: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
    if (start <= resultContext.numHits && end <= resultContext.numHits) {
      container.find("#loadingHits").show()
      container.find("#loadHits").hide()
      loading = true
      val route = s"/results/alignment/loadHits/$jobID"
      jQuery.ajax(
        js.Dynamic
          .literal(
            url = route,
            data = JSON.stringify(
              js.Dynamic.literal("start" -> start, "end" -> end, "resultName" -> resultName)
            ),
            contentType = "application/json",
            success = { (data: js.Any, _: js.Any, _: JQueryXHR) =>
              container.find(".alignmentTBody").append(data.asInstanceOf[String])
              shownHits = end
              if (shownHits != resultContext.numHits)
                container.find("#loadHits").show()
              checkboxes.initForContainer(container.find(".alignmentTBody"))
              container.find("#loadingHits").hide()
              loading = false
            },
            error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
              println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
              container.find(".alignmentTBody").append("Error loading Data.")
              container.find("#loadingHits").hide()
              loading = false
            },
            `type` = "POST"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
    }
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
