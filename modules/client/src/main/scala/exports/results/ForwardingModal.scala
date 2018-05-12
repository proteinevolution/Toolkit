package exports.results

import exports.facades.ForwardModalOptions
import exports.facades.JQueryPlugin._
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLElement, HTMLInputElement}
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("ForwardingModal")
class ForwardingModal(container: JQuery, toolName: String, jobID: String) {

  val $modal: JQuery = jQuery("#forwardModal")

  // find elements only once
  val $forwardSelect: JQuery         = $modal.find(".forwardSelect")
  val $selectionRadioBtnArea: JQuery = $modal.find(".radio-btn-selection-container")
  val $seqLengthRadioBtnArea: JQuery = $modal.find(".radio-btn-sequence-length-container")
  val $warning: JQuery               = $modal.find(".forwardWarning")

  var modalType: String = ""

  // data to extract from triggering links
  var defaultTool: String   = ""
  var resultName: String    = ""
  var forwardIssuer: String = ""

  // register open links
  container
    .find(".forwardModalOpenBtn")
    .off("click")
    .on("click", { btn: HTMLElement =>
        {
          val $btn = jQuery(btn)
          // read values from button
          defaultTool = if (!js.isUndefined($btn.data("default-tool"))) $btn.data("default-tool").toString else ""
          resultName = if (!js.isUndefined($btn.data("result-name"))) $btn.data("result-name").toString else ""
          forwardIssuer = if (!js.isUndefined($btn.data("forward-issuer"))) $btn.data("forward-issuer").toString else ""
          val newModalType: String = $btn.data("modal-type").toString
          if (newModalType != modalType) {
            requestModalOptions(newModalType)
          } else {
            $modal.foundation("open")
          }
        }
      }: js.ThisFunction
    )

  // register open event to initialize the modal
  $modal.off("open.zf.reveal").on(
    "open.zf.reveal",
    () => {
      $forwardSelect.value(defaultTool)
      $forwardSelect.find(".alignmentOptions").show()
      $forwardSelect.find(".multiSeqOptions").hide()
      $selectionRadioBtnArea.find(".forwardEvalueNormalInput").value("0.001")
      $selectionRadioBtnArea.find(".selectedHitsRadioBtn").prop("checked", true)
      $seqLengthRadioBtnArea.find(".sliderRegionRadioBtn").prop("checked", true)
      $warning.hide()
    }
  )

  def requestModalOptions(newModalType: String): Unit = {
    jQuery
      .ajax(
        js.Dictionary(
            "url" -> s"/forwardModal/$toolName/$newModalType"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((data: js.Dictionary[Any]) => {
        modalType = newModalType
        setupModalWithOptions(data.asInstanceOf[ForwardModalOptions])
        $modal.foundation("open")
      })
      .fail((jqXHR: JQueryXHR, textStatus: String, errorThrown: js.Any) => {
        println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrown")
      })
  }

  def setupModalWithOptions(options: ForwardModalOptions): Unit = {
    // detach children first to not lose reference
    $selectionRadioBtnArea.hide()
    $seqLengthRadioBtnArea.hide()
    // show radio btns
    if (options.showRadioBtnSelection) {
      // show and set heading
      $selectionRadioBtnArea.show().find(".forward-modal-heading").text(options.heading)
    }
    if (options.showRadioBtnSequenceLength) {
      $seqLengthRadioBtnArea.show()
      setupRadioBtnSeqLength()
    }

    // populate select
    $forwardSelect.empty().append("<option value=\"\">Select a tool</option>")
    options.alignmentOptions.map(
      tool => $forwardSelect.append(s"<option class='alignmentOptions' value='$tool'>$tool</option>")
    )
    options.multiSeqOptions.map(
      tool => $forwardSelect.append(s"<option class='multiSeqOptions' value='$tool'>$tool</option>")
    )

    // register on forward
    $modal.find(".forwardBtn").off("click").on(
      "click",
      modalType match {
        case "normal" =>
          () =>
            {
              val boolFullLength: Boolean = $seqLengthRadioBtnArea.find(".fullLengthRadioBtn").is(":checked")
              val boolEvalue: Boolean     = $selectionRadioBtnArea.find(".accordingEvalueRadioBtn").is(":checked")
              val selectedTool: String    = $forwardSelect.value().toString
              val evalue: String          = $selectionRadioBtnArea.find(".forwardEvalueNormalInput").value().toString
              if (selectedTool == "") {
                dom.window.alert("Please select a tool!")
              } else {
                if (boolEvalue && evalue == "") {
                  dom.window.alert("no evalue!")
                } else {
                  Forwarding.processResults(jobID, selectedTool, boolEvalue, evalue, boolFullLength)
                  $modal.foundation("close")
                }
              }
            }
        case "hhsuite" =>
          () =>
            {
              val selectedTool: String = $forwardSelect.value().toString
              if (selectedTool == "") {
                dom.window.alert("Please select a tool!")
              } else {
                Forwarding.processFiles(selectedTool, s"/files/$jobID/reduced.a3m")
                $modal.foundation("close")
              }
            }
        case "simple" =>
          () =>
            {
              val boolEvalue: Boolean  = $modal.find(".accordingEvalueRadioBtn").is(":checked")
              val selectedTool: String = $forwardSelect.value().toString
              val evalue: String       = $modal.find(".forwardEvalueNormalInput").value().toString
              if (selectedTool == "") {
                dom.window.alert("Please select a tool!")
              } else {
                if (evalue == "" && boolEvalue) {
                  dom.window.alert("no evalue!")
                } else {
                  Forwarding.processResults(jobID, selectedTool, boolEvalue, evalue, isFullLength = false)
                }
              }
              $modal.foundation("close")
            }
        case "simpler" =>
          () =>
            {
              val selectedTool: String = $forwardSelect.value().toString
              if (selectedTool == "") {
                dom.window.alert("Please select a tool!")
              } else {
                forwardIssuer.toUpperCase() match {
                  case "NORMAL" => Forwarding.processAlnResults(jobID, selectedTool, resultName)
                  case "UNCHECKED_LIST" =>
                    jQuery.LoadingOverlay("show")
                    jQuery.getJSON(
                      s"/files/$jobID/ids.json",
                      (data: js.Dynamic) => {
                        Forwarding.simple(selectedTool, data.ACC_IDS.join("\n").toString)
                        jQuery.LoadingOverlay("hide")
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
              $modal.foundation("close")
            }
        case _ =>
          () =>
            {
              dom.window.alert("Error! Forwarding does not work. Please refresh the page.")
            }
      }
    )
  }

  def setupRadioBtnSeqLength(): Unit = {
    $seqLengthRadioBtnArea.find("input[name='radio2']").off("change").on(
      "change", { radioBtn: HTMLInputElement =>
        {
          if (jQuery(radioBtn).hasClass("fullLengthRadioBtn")) {
            $warning.show()
            $forwardSelect.find(".alignmentOptions").hide()
            $forwardSelect.find(".multiSeqOptions").show()
          } else {
            $warning.hide()
            $forwardSelect.find(".alignmentOptions").show()
            $forwardSelect.find(".multiSeqOptions").hide()
          }
        }
        $forwardSelect.value("")
      }: js.ThisFunction
    )
  }

}
