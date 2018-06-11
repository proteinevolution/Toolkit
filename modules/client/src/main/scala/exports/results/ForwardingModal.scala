package exports.results

import de.proteinevolution.models.forwarding.ForwardModalOptions
import exports.facades.JQueryPlugin._
import helpers.SnakePickle
import org.scalajs.dom.raw.{ HTMLElement, HTMLInputElement }
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
  val $evalueInput: JQuery           = $selectionRadioBtnArea.find(".forwardEvalueNormalInput")
  val $fullLengthWarning: JQuery     = $modal.find(".full-length-warning")
  val $warning: JQuery               = $modal.find(".forward-warning")

  var modalType: String = ""

  // data to extract from triggering links
  var defaultTool: String   = ""
  var resultName: String    = ""
  var forwardIssuer: String = ""

  // register open links
  container
    .find(".forwardModalOpenBtn")
    .off("click")
    .on(
      "click", { btn: HTMLElement =>
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
      $forwardSelect.value(defaultTool).removeClass("invalid")
      $forwardSelect.find(".alignmentOptions").show()
      $forwardSelect.find(".multiSeqOptions").hide()
      $evalueInput.value("0.001").removeClass("invalid")
      $selectionRadioBtnArea.find(".selectedHitsRadioBtn").prop("checked", true)
      $seqLengthRadioBtnArea.find(".sliderRegionRadioBtn").prop("checked", true)
      $fullLengthWarning.hide()
      $warning.hide()
    }
  )

  implicit def forwardModalOptRW: SnakePickle.ReadWriter[ForwardModalOptions] = SnakePickle.macroRW

  def requestModalOptions(newModalType: String): Unit = {
    jQuery
      .ajax(
        js.Dictionary(
            "url" -> s"/forward/modal/$toolName/$newModalType"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((data: js.Dictionary[Any]) => {
        modalType = newModalType
        setupModalWithOptions(SnakePickle.read[ForwardModalOptions](js.JSON.stringify(data)))
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
      // bind focus handler to autoselect
      $evalueInput.off("focus")
        .on("focus", () => {
          $selectionRadioBtnArea.find(".accordingEvalueRadioBtn").prop("checked", true)
        })
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

    // remove invalid classes on edit
    $forwardSelect.off("change")
      .on("change", () => {
        if ($forwardSelect.value().toString.length > 0) {
          $forwardSelect.removeClass("invalid")
        } else {
          $forwardSelect.addClass("invalid")
        }
      })

    $evalueInput.off("keyup")
      .on("keyup", () => {
        if ($evalueInput.value().toString.length > 0) {
          $evalueInput.removeClass("invalid")
        } else {
          $evalueInput.addClass("invalid")
        }
      })

    // register on forward
    $modal.find(".forwardBtn")
      .off("click")
      .on(
        "click",
        modalType match {
          case "normal" =>
            () =>
              {
                val boolFullLength: Boolean = $seqLengthRadioBtnArea.find(".fullLengthRadioBtn").is(":checked")
                val boolEvalue: Boolean     = $selectionRadioBtnArea.find(".accordingEvalueRadioBtn").is(":checked")
                val selectedTool: String    = $forwardSelect.value().toString
                val evalue: String          = $evalueInput.value().toString.trim
                if (selectedTool == "") {
                  $forwardSelect.addClass("invalid")
                  warn("No tool selected!")
                } else {
                  if (boolEvalue && evalue == "") {
                    $evalueInput.addClass("invalid")
                    warn("No evalue!")
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
                  $forwardSelect.addClass("invalid")
                  warn("No tool selected!")
                } else {
                  jQuery.LoadingOverlay("show")
                  Forwarding.redirect(selectedTool, s"/files/$jobID/reduced.a3m")
                  $modal.foundation("close")
                  jQuery.LoadingOverlay("hide")
                }
              }
          case "simple" =>
            () =>
              {
                val boolEvalue: Boolean  = $modal.find(".accordingEvalueRadioBtn").is(":checked")
                val selectedTool: String = $forwardSelect.value().toString
                val evalue: String       = $evalueInput.value().toString.trim
                if (selectedTool == "") {
                  $forwardSelect.addClass("invalid")
                  warn("No tool selected!")
                } else {
                  if (evalue == "" && boolEvalue) {
                    $evalueInput.addClass("invalid")
                    warn("No evalue!")
                  } else {
                    Forwarding.processResults(jobID, selectedTool, boolEvalue, evalue, isFullLength = false)
                    $modal.foundation("close")
                  }
                }
              }
          case "simpler" =>
            () =>
              {
                val selectedTool: String = $forwardSelect.value().toString
                if (selectedTool == "") {
                  $forwardSelect.addClass("invalid")
                  warn("No tool selected!")
                } else {
                  forwardIssuer.toUpperCase match {
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
                      jQuery.LoadingOverlay("show")
                      Forwarding.redirect(selectedTool, s"/files/$jobID/$jobID.fas")
                      jQuery.LoadingOverlay("hide")
                    case "FILEVIEW" =>
                      jQuery.LoadingOverlay("show")
                      // resultName is actually the filename in this case
                      Forwarding.redirect(selectedTool, s"/files/$jobID/$resultName")
                      jQuery.LoadingOverlay("hide")
                  }
                  $modal.foundation("close")
                }
              }
          case _ =>
            () =>
              {
                warn("Error! Forwarding does not work. Please refresh the page.")
              }
        }
      )
  }

  def setupRadioBtnSeqLength(): Unit = {
    $seqLengthRadioBtnArea.find("input[name='radio2']")
      .off("change")
      .on(
        "change", { radioBtn: HTMLInputElement =>
          {
            if (jQuery(radioBtn).hasClass("fullLengthRadioBtn")) {
              $fullLengthWarning.show()
              $forwardSelect.find(".alignmentOptions").hide()
              $forwardSelect.find(".multiSeqOptions").show()
            } else {
              $fullLengthWarning.hide()
              $forwardSelect.find(".alignmentOptions").show()
              $forwardSelect.find(".multiSeqOptions").hide()
            }
          }
          $forwardSelect.value("")
        }: js.ThisFunction
      )
  }

  def warn(text: String): Unit = {
    $warning.text(text).show()
  }

}
