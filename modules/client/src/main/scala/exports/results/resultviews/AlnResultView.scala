package exports.results.resultviews

import exports.facades.ResultContext
import exports.results.models.ResultForm.MsaResultForm
import org.scalajs.jquery.{ JQuery, JQueryXHR }

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("AlnResultView")
class AlnResultView(
    val container: JQuery,
    val jobID: String,
    resultName: String,
    val hitsToLoad: Int,
    val resultContext: ResultContext
) extends ResultView {

  override def init(): Unit = {
    initScrollWatch()
    if (resultContext.numHits > 0) {
      commonBindEvents()
      showHits(0, hitsToLoad)
    }
  }

  override def showHits(
      start: Int,
      end: Int,
      successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null
  ): Unit = {
    internalShowHits(
      jobID,
      s"/results/alignment/loadHits/$jobID",
      MsaResultForm(start, end, resultName),
      container.find(".alignmentTBody"),
      successCallback
    )
  }
}
