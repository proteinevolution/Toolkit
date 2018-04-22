package exports.results

import exports.extensions.JQueryExtensions
import exports.facades.{JQueryPosition, ResultContext}
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ResultViewHelper")
object ResultViewHelper {

  var container: JQuery = _
  @JSExport
  var jobID: String = _
  @JSExport
  var shownHits: Int = _
  @JSExport
  var wrapped: Boolean = _
  @JSExport
  var colorAAs: Boolean = _
  @JSExport
  var resultContext: ResultContext = _
  @JSExport
  var loading: Boolean = false

  private var checkboxes: Checkboxes = _
  private var hitsSlider: HitsSlider = _

  @JSExport
  def init(container: JQuery, jobID: String, wrapped: Boolean, shownHits: Int, colorAAs: Boolean, resultContext: ResultContext): Unit = {
    this.jobID = jobID
    this.shownHits = if (shownHits > resultContext.numHits) resultContext.numHits else shownHits
    this.wrapped = wrapped
    this.container = container
    this.colorAAs = colorAAs
    this.resultContext = resultContext
    this.checkboxes = new Checkboxes(container)
    this.hitsSlider = new HitsSlider(container)

    setupBlastVizTooltipster()
    js.Dynamic.global.$.LoadingOverlay("hide")
    js.Dynamic.global.followScroll(dom.document)

    // add slider val
    container.find(".slider").on("moved.zf.slider", () => {
      container.find("#lefthandle").html(dom.document.getElementById("hidden1").asInstanceOf[HTMLInputElement].value).css(js.Dictionary(
        "color" -> "white",
        "font-weight" -> "bold",
        "padding-left" -> "2px"
      ))
      container.find("#righthandle").html(dom.document.getElementById("hidden2").asInstanceOf[HTMLInputElement].value).css(js.Dictionary(
        "color" -> "white",
        "font-weight" -> "bold",
        "padding-left" -> "2px"
      ))
    })

    if (resultContext.numHits > 0) {
      val wrap = container.find("#wrap")
      if (wrapped) {
        wrap.text("Unwrap Seqs").addClass("colorToggleBar")
      }
      if(colorAAs) {
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

  @JSExport
  def initAln(container: JQuery, jobID: String, resultName: String, shownHits: Int, format: String, resultContext: ResultContext): Unit = {
    this.jobID = jobID
    this.shownHits = if (shownHits > resultContext.numHits) resultContext.numHits else shownHits
    this.container = container
    this.resultContext = resultContext
    this.checkboxes = new Checkboxes(container)
    this.hitsSlider = new HitsSlider(container)

    if (resultContext.numHits > 0) {
      bindEvents()
      showHitsAln(0, this.shownHits, resultName, format)
    }
  }

  @JSExport
  def initClustal(container: JQuery, jobID: String, resultName: String, colorAAs: Boolean, resultContext: ResultContext): Unit = {
    this.jobID = jobID
    this.container = container
    this.shownHits = if (shownHits > resultContext.numHits) resultContext.numHits else shownHits
    this.resultContext = resultContext
    this.colorAAs = colorAAs
    this.checkboxes = new Checkboxes(container)

    if (resultContext.numHits > 0) {
      container.find(".colorAA").on("click", () => {
        this.colorAAs = !this.colorAAs
        container.find(".colorAA").toggleClass("colorToggleBar")
        container.find("#resultTable").empty()
        showHitsClustal(0, this.shownHits, resultName)
      })
      container.find(".selectAllSeqBar").on("click", () => {
        container.find(".selectAllSeqBar").toggleClass("colorToggleBar")
        JQueryExtensions.toggleText(container.find(".selectAllSeqBar"), "Select all", "Deselect all")
        checkboxes.toggleAll(resultContext.numHits)
      })
      showHitsClustal(0, this.shownHits, resultName)
    }
  }

  def setupBlastVizTooltipster(): Unit = {
    // add tooltipster to visualization
    val blastVizArea = container.find("#blastviz").find("area")
    blastVizArea.asInstanceOf[exports.facades.JQuery].tooltipster(
      js.Dictionary(
        "theme" -> js.Array("tooltipster-borderless",
          "tooltipster-borderless-customized"),
        "position" -> "bottom",
        "animation" -> "fade",
        "contentAsHTML" -> true,
        "debug" -> false,
        "maxWidth" -> blastVizArea.innerWidth() * 0.6
      ))
  }

  def bindEvents(): Unit = {
    container.find("#wrap").on("click", () => {
      toggleIsWrapped()
    })
    container.find("#resubmitSection").on("click", () => {
      hitsSlider.resubmit(resultContext.query.seq, '>' + resultContext.query.accession)
    })
    container.find(".selectAllSeqBar").on("click", () => {
      container.find(".selectAllSeqBar").toggleClass("colorToggleBar")
      JQueryExtensions.toggleText(container.find(".selectAllSeqBar"), "Select all", "Deselect all")
      checkboxes.toggleAll(resultContext.numHits)
    })
    container.find(".colorAA").on("click", () => {
      toggleAlignmentColoring()
    })
    container.find("#visualizationScroll").on("click", () => {
      scrollToSection("visualization")
    })
    container.find("#hitlistScroll").on("click", () => {
      scrollToSection("hitlist")
    })
    container.find("#alignmentsScroll").on("click", () => {
      scrollToSection("alignments")
    })
  }

  @JSExport
  def showHits(start: Int,
               end: Int,
               successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
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
              dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
              loading = false
            },
            `type` = "POST"
          ).asInstanceOf[JQueryAjaxSettings]
      )
    }
  }

  def showHitsAln(start: Int, end: Int, resultName: String, format: String): Unit = {
    if (start <= resultContext.numHits && end <= resultContext.numHits) {
      container.find("#loadingHits").show()
      container.find("#loadHits").hide()
      loading = true
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
              dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
              container.find(".alignmentTBody").append("Error loading Data.")
              container.find("#loadingHits").hide()
              loading = false
            },
            `type` = "POST"
          ).asInstanceOf[JQueryAjaxSettings]
      )
    }
  }

  def showHitsClustal(start: Int, end: Int, resultName: String): Unit = {
    loading = true
    container.find("#loadingHits").show()
    container.find("#loadHits").hide()
    jQuery.ajax(
      js.Dynamic
        .literal(
          url = "/results/alignment/clustal/" + jobID,
          method = "POST",
          contentType = "application/json",
          data = JSON.stringify(js.Dictionary("color" -> colorAAs, "resultName" -> resultName)),
          success = { (data: js.Any, _: js.Any, _: JQueryXHR) =>
            container.find("#resultTable").append(data)
            shownHits = end
            if (shownHits != resultContext.numHits)
              container.find("#loadHits").show()
            checkboxes.initForContainer(container.find("#resultTable"))
            container.find("#loadingHits").hide()
            js.Dynamic.global.$.LoadingOverlay("hide")
            loading = false
          },
          error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
            dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
            container.find("#resultTable").append("Error loading Data.")
            container.find("#loadingHits").hide()
            loading = false
          }
        ).asInstanceOf[JQueryAjaxSettings]
    )
  }

  @JSExport
  def getSelectedValues: js.Array[Int] = {
    checkboxes.getChecked
  }

  def toggleAlignmentColoring(): Unit = {
    colorAAs = !colorAAs
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

  @JSExport
  def scrollToHit(id: Int): Unit = {
    val elem =
      if (container.find("#tool-tabs").hasClass("fullscreen"))
        "#tool-tabs"
      else
        "html, body"
    if (id > js.Dynamic.global.shownHits.asInstanceOf[Int]) {
      js.Dynamic.global.$.LoadingOverlay("show")
      showHits(
        shownHits,
        id,
        (_: js.Any, _: js.Any, _: JQueryXHR) => {
          js.Dynamic.global.shownHits = id
          jQuery(elem).animate(
            js.Dictionary("scrollTop" -> (container.find(".aln[value='" + id + "']").offset().asInstanceOf[JQueryPosition].top.asInstanceOf[Double] - 100)),
            1,
            "swing",
            null
          )
        }
      )
      jQuery(elem)
        .animate(js.Dynamic.literal("scrollTop" -> (container.find(".aln[value='" + id + "']").offset().asInstanceOf[JQueryPosition].top - 100.toDouble)), 1)
      js.Dynamic.global.$.LoadingOverlay("hide")
      js.Dynamic.global.shownHits = id
    } else {
      jQuery(elem)
        .animate(js.Dynamic.literal("scrollTop" -> (container.find(".aln[value='" + id + "']").offset().asInstanceOf[JQueryPosition].top - 100.toDouble)), 1)
    }
  }

  def scrollToSection(name: String): Unit = {
    val elem =
      if (container.find("#tool-tabs").hasClass("fullscreen"))
        "#tool-tabs"
      else
        "html, body"
    val _pos = container.find("#" + name).offset().asInstanceOf[JQueryPosition].top
    val pos =
      if (container.find("#tool-tabs").hasClass("fullscreen"))
        jQuery(elem).scrollTop()
      else
        25.toDouble
    jQuery(elem)
      .animate(js.Dynamic.literal("scrollTop" -> (_pos + pos)), "fast")
  }

}
