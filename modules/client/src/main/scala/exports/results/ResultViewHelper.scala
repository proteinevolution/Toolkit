package exports.results

import org.querki.jquery.$
import org.scalajs.dom
import org.scalajs.dom.Element
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ResultViewHelper")
object ResultViewHelper {

  @JSExport
  def showHits(start: Int, end: Int, isWrapped: Boolean, isColored: Boolean, numHits: Int, jobID: String, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
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
              js.Dynamic.global.$.LoadingOverlay("hide")
              if(successCallback != null) successCallback(data, textStatus, jqXHR)
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

  @JSExport
  def showHitsAln(start: Int, end: Int, numHits: Int, jobID: String, resultName: String, format: String): Unit = {
    if (start <= numHits && end <= numHits) {
      $("#loadingHits").show()
      val route = format match {
        case "clu" => s"/results/alignment/clustal/$jobID"
        case _ => s"/results/alignment/loadHits/$jobID"
      }
      jQuery.ajax(
        js.Dynamic
          .literal(
            url = route,
            data = JSON.stringify(
              js.Dynamic.literal("start" -> start, "end" -> end, "resultName" -> resultName)
            ),
            contentType = "application/json",
            success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
              $(".alignmentTBody").append(data.asInstanceOf[String])
              $("#loadingHits").hide()
            },
            error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
              dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
              $(".alignmentTBody").append("Error loading Data.")
              $("#loadingHits").hide()
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
        (el: Element, _: Int) => jQuery(el).prop("checked", true)
      )
      val numHits = js.Dynamic.global.numHits.asInstanceOf[Int]
      val range = 1 to numHits + 1
      js.Dynamic.global.checkboxes = range.toJSArray
    } else {
      deselectAll()
    }
  }

  @JSExport
  def deselectAll(): Unit = {
    $("input:checkbox[name='alignment_elem']").each(
      (el: Element, _: Int) => jQuery(el).prop("checked", false)
    )
    js.Dynamic.global.checkboxes = Seq.empty.toJSArray
  }

  @JSExport
  def toggleAlignmentColoring(jobID: String,
                              shownHits: Int,
                              isWrapped: Boolean,
                              isColored: Boolean,
                              numHits: Int): Unit = {

    js.Dynamic.global.$.LoadingOverlay("show")
    $(".colorAA").toggleClass("colorToggleBar")
    $("#alignmentTable").empty()
    showHits(0, shownHits, isWrapped, !isColored, numHits, jobID)
  }

  @JSExport
  def toggleIsWrapped(jobID: String, shownHits: Int, numHits: Int, isWrapped: Boolean, isColored: Boolean): Unit = {
    $("#wrap").toggleClass("colorToggleBar")
    jQuery("#wrap").toggleText("Unwrap Seqs", "Wrap Seqs")
    $("#alignmentTable").empty()
    showHits(0, shownHits, !isWrapped, isColored, numHits, jobID)
  }

  @JSExport
  def scrollToHit(id: Int): Unit = {
    val elem =
      if ($("#tool-tabs").hasClass("fullscreen"))
        "#tool-tabs"
      else
        "html, body"
    if (id > js.Dynamic.global.shownHits.asInstanceOf[Int]) {
      js.Dynamic.global.$.LoadingOverlay("show")
      showHits(
        js.Dynamic.global.shownHits.asInstanceOf[Int],
        id,
        js.Dynamic.global.wrapped.asInstanceOf[Boolean],
        isColored = false,
        js.Dynamic.global.numHits.asInstanceOf[Int],
        js.Dynamic.global.jobID.asInstanceOf[String],
        (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) => {
          js.Dynamic.global.shownHits = id
          $(elem).animate(js.Dictionary("scrollTop" -> ($(".aln[value='" + id + "']").offset().top.asInstanceOf[Double] - 100)), 1, "swing", null)
        }
      )
    } else {
      $(elem).animate(js.Dictionary("scrollTop" -> ($(".aln[value='" + id + "']").offset().top.asInstanceOf[Double] - 100)), 1, "swing", null)
    }

  }

  @JSExport
  def scrollToSection(name: String): Unit = {
    val elem =
      if ($("#tool-tabs").hasClass("fullscreen"))
        "#tool-tabs"
      else
        "html, body"
    val _pos = $("#" + name).offset().top
    val pos =
      if ($("#tool-tabs").hasClass("fullscreen"))
        $(elem).scrollTop()
      else
        25.toDouble
    $(elem).animate(js.Dictionary("scrollTop" -> (_pos + pos)), "fast", "swing", null)
  }

}
