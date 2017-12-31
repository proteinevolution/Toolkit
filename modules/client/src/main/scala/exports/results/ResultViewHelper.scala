package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.jquery._
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.Element
import org.querki.jquery.$
import scala.scalajs.js.JSON

@JSExportTopLevel("ResultViewHelper")
object ResultViewHelper {

  @JSExport
  def showHits(start: Int, end: Int, isWrapped: Boolean, isColored: Boolean, numHits: Int, jobID: String): Unit = {
    if (start <= numHits && end <= numHits) {
      jQuery("#loadingHits").show()
      jQuery("#loadHits").hide()
      jQuery.ajax(
        js.Dynamic
          .literal(
            url = s"/results/loadHits/$jobID",
            data = JSON.stringify(
              js.Dynamic.literal("start" -> start, "end" -> end, "wrapped" -> isWrapped, "isColor" -> isColored)
            ),
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

  implicit class JQueryToggleText(val self: JQuery) extends AnyVal {
    def toggleText(a: String, b: String): self.type = {
      if (self.text() == b)
        self.text(a)
      else
        self.text(b)
      self
    }
  }

  @JSExport
  def toggleAllCheckboxes(): Unit = {
    jQuery(".selectAllSeqBar").toggleClass("colorToggleBar")
    jQuery(".selectAllSeqBar").toggleText("Select all", "Deselect all")
    if ($("input:checkbox[name='alignment_elem']:checked").length != $("input:checkbox[name='alignment_elem']").length) {
      $("input:checkbox[name='alignment_elem']").each(
        (el: Element, i: Int) => jQuery(el).prop("checked", true)
      )
    } else {
      deselectAll()
    }
  }

  @JSExport
  def deselectAll(): Unit = {
    $("input:checkbox[name='alignment_elem']").each(
      (el: Element, i: Int) => jQuery(el).prop("checked", false)
    )
  }

}
