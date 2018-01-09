package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.jquery._
import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.Element
import org.querki.jquery.$
import scala.scalajs.js.JSON
import scala.scalajs.js.JSConverters._

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
              js.Dynamic.global.$.LoadingOverlay("hide")
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
      $("#loadHitsAln").hide()
      $("#loadingHits").show()
      val route = format match {
        case "clu" => s"/results/alignment/clustal/$jobID"
        case _     => s"/results/alignment/loadHits/$jobID"
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
              if (js.Dynamic.global.shownHits.asInstanceOf[Int] != numHits)
                $("#loadHitsAln").show()
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

  // TODO think about how to get twirl templates immutable
  @JSExport
  def showHitsManually(): Unit = {
    if (!js.Dynamic.global.loading.asInstanceOf[Boolean]) {
      js.Dynamic.global.end = js.Dynamic.global.shownHits.asInstanceOf[Int] + 100
      if (js.Dynamic.global.end.asInstanceOf[Int] < js.Dynamic.global.numHits.asInstanceOf[Int])
        js.Dynamic.global.end = js.Dynamic.global.end
      else
        js.Dynamic.global.end = js.Dynamic.global.numHits
      if (js.Dynamic.global.shownHits.asInstanceOf[Int] != js.Dynamic.global.end.asInstanceOf[Int]) {
        showHits(
          js.Dynamic.global.shownHits.asInstanceOf[Int],
          js.Dynamic.global.end.asInstanceOf[Int],
          js.Dynamic.global.wrapped.asInstanceOf[Boolean],
          isColored = false,
          js.Dynamic.global.numHits.asInstanceOf[Int],
          js.Dynamic.global.jobID.asInstanceOf[String]
        )
      }
      js.Dynamic.global.shownHits = js.Dynamic.global.end
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
      val numHits = js.Dynamic.global.numHits.asInstanceOf[Int]
      val range   = 1 to numHits + 1
      js.Dynamic.global.checkboxes = range.toJSArray
    } else {
      deselectAll()
    }
  }

  @JSExport
  def deselectAll(): Unit = {
    $("input:checkbox[name='alignment_elem']").each(
      (el: Element, i: Int) => jQuery(el).prop("checked", false)
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
      js.Dynamic.global.$.LoadingOverlay.show()
      showHits(
        js.Dynamic.global.shownHits.asInstanceOf[Int],
        id,
        js.Dynamic.global.wrapped.asInstanceOf[Boolean],
        isColored = false,
        js.Dynamic.global.numHits.asInstanceOf[Int],
        js.Dynamic.global.jobID.asInstanceOf[String]
      )
      js.Dynamic.global
        .$(elem)
        .animate(js.Dynamic.literal("scrollTop" -> ($(".aln[value='" + id + "']").offset().top - 100.toDouble)), 1)
      js.Dynamic.global.$.LoadingOverlay("hide")
      js.Dynamic.global.shownHits = id
    } else {
      js.Dynamic.global
        .$(elem)
        .animate(js.Dynamic.literal("scrollTop" -> ($(".aln[value='" + id + "']").offset().top - 100.toDouble)), 1)
    }
  }

}
