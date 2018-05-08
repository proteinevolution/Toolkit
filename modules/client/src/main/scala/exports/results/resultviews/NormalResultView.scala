package exports.results.resultviews

import exports.extensions.JQueryExtensions
import exports.facades.ResultContext
import exports.results.DataTables
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery.{ jQuery, JQuery, JQueryXHR }

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.JSExportTopLevel

import exports.facades.JQueryPlugin.jqPlugin

@JSExportTopLevel("NormalResultView")
class NormalResultView(container: JQuery,
                       jobID: String,
                       val tempShownHits: Int,
                       var wrapped: Boolean,
                       var colorAAs: Boolean,
                       val resultContext: ResultContext)
    extends ResultView(container) {

  override def init(): Unit = {

    scrollUtil.followScroll(jQuery(dom.document))

    // add slider val
    container
      .find(".slider")
      .on(
        "moved.zf.slider",
        () => {
          container
            .find("#lefthandle")
            .html(dom.document.getElementById("hidden1").asInstanceOf[HTMLInputElement].value)
            .css(
              js.Dictionary(
                "color"        -> "white",
                "font-weight"  -> "bold",
                "padding-left" -> "2px"
              )
            )
          container
            .find("#righthandle")
            .html(dom.document.getElementById("hidden2").asInstanceOf[HTMLInputElement].value)
            .css(
              js.Dictionary(
                "color"        -> "white",
                "font-weight"  -> "bold",
                "padding-left" -> "2px"
              )
            )
        }
      )

    if (resultContext.numHits > 0) {
      val wrap = container.find("#wrap")
      if (wrapped) {
        wrap.text("Unwrap Seqs").addClass("colorToggleBar")
      }
      if (colorAAs) {
        container.find(".colorAA").addClass("colorToggleBar")
      }
      bindEvents()
      new DataTables(resultContext.toolName).config(jobID, resultContext.numHits, () => {
        if (resultContext.toolName.equals("hhomp"))
          js.undefined
        else {
          checkboxes.initForContainer(jQuery("#htb"))
        }
      })

      // hide colorAAs button initially
      container.find(".colorAA").hide()

      if (resultContext.toolName.equals("psiblast")) {
        container.find(".selectAllSeqBar").addClass("colorToggleBar").text("Deselect all")
        checkboxes.selectAll(resultContext.belowEvalThreshold - 1)
      }
      showHits(0, this.shownHits)
      setupBlastVizTooltipster()
      wrap.hide()
    }
  }

  def setupBlastVizTooltipster(): Unit = {
    // add tooltipster to visualization
    val blastVizArea = container.find("#blastviz").find("area")
    blastVizArea
      .tooltipster(
        js.Dictionary(
          "theme"         -> js.Array("tooltipster-borderless", "tooltipster-borderless-customized"),
          "position"      -> "bottom",
          "animation"     -> "fade",
          "contentAsHTML" -> true,
          "debug"         -> false,
          "maxWidth"      -> blastVizArea.innerWidth() * 0.6
        )
      )
    hitsSlider.show(resultContext.query.seq.length, resultContext.firstQueryStart, resultContext.firstQueryEnd)
  }

  override def bindEvents(): Unit = {
    container
      .find("#wrap")
      .on("click", () => {
        toggleIsWrapped()
      })
    container
      .find("#resubmitSection")
      .on("click", () => {
        hitsSlider.resubmit(resultContext.query.seq, '>' + resultContext.query.accession)
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
    container
      .find(".colorAA")
      .on("click", () => {
        toggleAlignmentColoring()
      })
    container
      .find("#visualizationScroll")
      .on("click", () => {
        scrollToSection("visualization")
      })
    container
      .find("#hitlistScroll")
      .on("click", () => {
        scrollToSection("hitlist")
      })
    container
      .find("#alignmentsScroll")
      .on("click", () => {
        scrollToSection("alignments")
      })
  }

  override def showHits(start: Int, end: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
    internalShowHits(
      s"/results/loadHits/$jobID",
      JSON.stringify(
        js.Dictionary("start" -> start, "end" -> end, "wrapped" -> wrapped, "isColor" -> colorAAs)
      ),
      container.find("#alignmentTable"),
      start,
      end,
      successCallback
    )
  }

  def toggleAlignmentColoring(): Unit = {
    this.colorAAs = !this.colorAAs
    container.find(".colorAA").toggleClass("colorToggleBar")
    container.find("#alignmentTable").empty()
    showHits(0, shownHits)
  }

  def toggleIsWrapped(): Unit = {
    wrapped = !wrapped
    container.find("#wrap").toggleClass("colorToggleBar")
    JQueryExtensions.toggleText(container.find("#wrap"), "Unwrap Seqs", "Wrap Seqs")
    container.find("#alignmentTable").empty()
    showHits(0, shownHits)
  }

}
