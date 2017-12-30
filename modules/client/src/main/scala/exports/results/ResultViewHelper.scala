package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.jquery._
import scala.scalajs.js
import org.scalajs.dom

@JSExportTopLevel("ResultViewHelper")
class ResultViewHelper() {

  @JSExport
  def showHits(start: Int, end: Int, isWrapped: Boolean, isColored: Boolean, numHits: Int, jobID: String): Unit = {
    if (start <= numHits && end <= numHits) {
      jQuery("#loadingHits").show()
      jQuery("#loadHits").hide()
      jQuery.ajax(
        js.Dynamic
          .literal(
            url = s"/results/loadHits/$jobID",
            data = js.Dynamic.literal("start" -> start, "end" -> end, "wrapped" -> isWrapped, "isColor" -> isColored),
            contentType = "application/json",
            success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
              jQuery("#alignmentTable").append(data)
              jQuery("#loadingHits").hide()
              if (js.Dynamic.global.shownHits.asInstanceOf[Int] != numHits)
                jQuery("#loadHits").show()
              js.Dynamic.global.linkCheckboxes()
              js.Dynamic.global.$("#alignments").floatingScroll("init")
            },
            error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
              dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
            },
            `type` = "POST"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )

    }
  }
}
