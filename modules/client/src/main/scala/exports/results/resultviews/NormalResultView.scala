package exports.results.resultviews

import exports.extensions.JQueryExtensions
import exports.facades.ResultContext
import exports.results.DataTables
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery.{ jQuery, JQuery, JQueryAjaxSettings, JQueryXHR }

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("NormalResultView")
class NormalResultView(container: JQuery,
                       jobID: String,
                       tempShownHits: Int,
                       var wrapped: Boolean,
                       var colorAAs: Boolean,
                       resultContext: ResultContext)
    extends ResultView(container, jobID, tempShownHits, resultContext) {

  override def init(): Unit = {

    setupBlastVizTooltipster()
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

      if (resultContext.toolName.equals("psiblast")) {
        container.find(".selectAllSeqBar").addClass("colorToggleBar").text("Deselect all")
        checkboxes.selectAll(resultContext.belowEvalThreshold)
      }
      hitsSlider.show(resultContext.query.seq.length, resultContext.firstQueryStart, resultContext.firstQueryEnd)
      showHits(0, this.shownHits)
      wrap.hide()
    }
  }

  def setupBlastVizTooltipster(): Unit = {
    // add tooltipster to visualization
    val blastVizArea = container.find("#blastviz").find("area")
    blastVizArea
      .asInstanceOf[exports.facades.JQuery]
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
    if (start <= resultContext.numHits && end <= resultContext.numHits) {
      container.find("#loadingHits").show()
      container.find("#loadHits").hide()
      loading = true
      jQuery.ajax(
        js.Dynamic
          .literal(
            url = s"/results/loadHits/$jobID",
            data = JSON.stringify(
              js.Dynamic.literal("start" -> start, "end" -> end, "wrapped" -> wrapped, "isColor" -> colorAAs)
            ),
            contentType = "application/json",
            success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
              container.find("#alignmentTable").append(data)
              container.find("#loadingHits").hide()
              shownHits = end
              if (shownHits != resultContext.numHits)
                container.find("#loadHits").show()
              checkboxes.initForContainer(container.find(".result-panel"))
              js.Dynamic.global.$("#alignments").floatingScroll("init")
              js.Dynamic.global.$.LoadingOverlay("hide")
              if (successCallback != null) successCallback(data, textStatus, jqXHR)
              loading = false
            },
            error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
              println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
              loading = false
            },
            `type` = "POST"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
    }
  }

  def toggleAlignmentColoring(): Unit = {
    this.colorAAs = !this.colorAAs
    js.Dynamic.global.$.LoadingOverlay("show")
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
