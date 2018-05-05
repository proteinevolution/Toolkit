package exports.results

import org.scalajs.dom
import org.scalajs.dom.raw.HTMLInputElement
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("ForwardingModalHelper")
object ForwardingModalHelper {

  @JSExport
  def bindNormal($modal: JQuery, jobID: String, defaultTool: String): Unit = {
    val $forwardSelect: JQuery = $modal.find(".forwardSelect")
    $modal.asInstanceOf[exports.facades.JQuery].foundation()

    $modal.on(
      "open.zf.reveal",
      () => {
        $forwardSelect.value(defaultTool)
        $modal.find(".forwardWarning").css(js.Dictionary("display" -> "none"))
        $modal.find(".forwardEvalueNormalInput").value("0.001")
        $modal.find(".selectedHitsRadioBtn").prop("checked", true)
        $modal.find(".sliderRegionRadioBtn").prop("checked", true)
        $forwardSelect.find(".alignmentOptions").show()
        $forwardSelect.find(".multiSeqOptions").hide()
      }
    )

    $modal.find(".forwardBtn").on(
      "click",
      () => {
        val boolFullLength: Boolean = $modal.find(".fullLengthRadioBtn").is(":checked")
        val boolEvalue: Boolean     = $modal.find(".accordingEvalueRadioBtn").is(":checked")
        val selectedTool: String    = $forwardSelect.value().toString
        val evalue: String          = $modal.find(".forwardEvalueNormalInput").value().toString
        if (selectedTool == "") {
          dom.window.alert("Please select a tool!")
        } else {
          Forwarding.processResults(jobID, selectedTool, boolEvalue, evalue, boolFullLength)
          if (evalue == "") {
            dom.window.alert("no evalue!")
          } else {
            $modal.asInstanceOf[exports.facades.JQuery].foundation("close")
          }
        }
      }
    )

    $modal.find("input[name='radio2']").on(
      "change", { radioBtn: HTMLInputElement =>
        {
          if (jQuery(radioBtn).hasClass("fullLengthRadioBtn")) {
            $modal.find(".forwardWarning").css(js.Dictionary("display" -> "inherit"))
            $forwardSelect.find(".alignmentOptions").hide()
            $forwardSelect.find(".multiSeqOptions").show()
            $forwardSelect.value("")
          } else {
            $modal.find(".forwardWarning").css(js.Dictionary("display" -> "none"))
            $forwardSelect.find(".alignmentOptions").show()
            $forwardSelect.find(".multiSeqOptions").hide()
            $forwardSelect.value(defaultTool)
          }
        }
      }: js.ThisFunction
    )
  }

  @JSExport
  def bindSimple($modal: JQuery, jobID: String): Unit = {
    val $forwardSelect: JQuery = $modal.find(".forwardSelect")

    $modal.asInstanceOf[exports.facades.JQuery].foundation()

    $modal.on(
      "open.zf.reveal",
      () => {
        $forwardSelect.value("")
        $modal.find(".forwardEvalueNormalInput").value("0.001")
        $modal.find(".selectedHitsRadioBtn").prop("checked", true)
        $forwardSelect.find(".alignmentOptions").show()
        $forwardSelect.find(".multiSeqOptions").hide()
      }
    )

    $modal.find(".forwardBtn").on(
      "click",
      () => {
        val boolEvalue: Boolean  = $modal.find(".accordingEvalueRadioBtn").is(":checked")
        val selectedTool: String = $forwardSelect.value().toString
        val evalue: String       = $modal.find(".forwardEvalueNormalInput").value().toString
        if (selectedTool == "") {
          dom.window.alert("Please select a tool!")
        } else {
          Forwarding.processResults(jobID, selectedTool, boolEvalue, evalue, isFullLength = false)
          if (evalue == "") {
            dom.window.alert("no evalue!")
          }
        }
        $modal.asInstanceOf[exports.facades.JQuery].foundation("close")
      }
    )
  }

  @JSExport
  def bindSimpler($modal: JQuery, jobID: String, resultName: String, forwardIssuer: String): Unit = {
    val $forwardSelect: JQuery = $modal.find(".forwardSelect")
    $modal.asInstanceOf[exports.facades.JQuery].foundation()

    $modal.on("open.zf.reveal", () => {
      $forwardSelect.value("")
      $forwardSelect.find(".alignmentOptions").show()
      $forwardSelect.find(".multiSeqOptions").hide()
    })

    $modal.find(".forwardBtn").on(
      "click",
      () => {
        val selectedTool: String = $forwardSelect.value().toString
        if (selectedTool == "") {
          dom.window.alert("Please select a tool!")
        } else {
          forwardIssuer match {
            case "NORMAL" => Forwarding.processAlnResults(jobID, selectedTool, resultName)
            case "UNCHECKED_LIST" =>
              jQuery.asInstanceOf[exports.facades.JQuery].LoadingOverlay("show")
              jQuery.getJSON(
                s"/files/$jobID/ids.json",
                (data: js.Dynamic) => {
                  Forwarding.simple(selectedTool, data.ACC_IDS.join("\n").toString)
                  jQuery.asInstanceOf[exports.facades.JQuery].LoadingOverlay("hide")
                }
              )
            case "PATTERN_SEARCH" =>
              val fileURL = s"/files/$jobID/$jobID.fas"
              Forwarding.redirect(selectedTool, fileURL)
            case "FILEVIEW" =>
              Forwarding
                .processFiles(selectedTool, s"/files/$jobID/$resultName") // resultName is actually the filename in this case
          }
        }
        $modal.asInstanceOf[exports.facades.JQuery].foundation("close")
      }
    )

    $modal.find("input[name='radio2']").on(
      "change", { radioBtn: HTMLInputElement =>
        {
          if (jQuery(radioBtn).hasClass("fullLengthRadioBtn")) {
            $modal.find(".forwardWarning").css(js.Dictionary("display" -> "inherit"))
            $forwardSelect.find(".alignmentOptions").hide()
            $forwardSelect.find(".multiSeqOptions").show()
          } else {
            $modal.find(".forwardWarning").css(js.Dictionary("display" -> "none"))
            $forwardSelect.find(".alignmentOptions").show()
            $forwardSelect.find(".multiSeqOptions").hide()
          }
        }
      }: js.ThisFunction
    )
  }

  @JSExport
  def bindHHSuite($modal: JQuery, jobID: String): Unit = {
    val $forwardSelect: JQuery = $modal.find(".forwardSelect")

    $modal.asInstanceOf[exports.facades.JQuery].foundation()

    $modal.bind("open.zf.reveal", () => {
      $forwardSelect.value("")
    })

    $modal.find(".forwardBtn").on(
      "click",
      () => {
        val selectedTool: String = $forwardSelect.value().toString
        if (selectedTool == "") {
          dom.window.alert("Please select a tool!")
        } else {
          Forwarding.processFiles(selectedTool, s"/files/$jobID/reduced.a3m")
          $modal.asInstanceOf[exports.facades.JQuery].foundation("close")
        }
      }
    )
  }

}
