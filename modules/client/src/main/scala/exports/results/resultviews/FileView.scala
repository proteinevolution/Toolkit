package exports.results.resultviews

import org.scalajs.jquery.jQuery

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.document
import com.tgf.pizza.scalajs.mithril._

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import exports.facades.JQueryPlugin._
import exports.results.DownloadHelper

import scala.scalajs.js.UndefOr

@JSExportTopLevel("FileView")
class FileView() {

  @JSExport("apply")
  def apply(jobID: String, fileName: String, resultName: String): Unit = {
    document
      .getElementById("download_alignment")
      .addEventListener("click", (_: dom.Event) => {
        downloadFile(jobID, fileName, resultName)
      }, useCapture = false)
    document
      .getElementById("collapseMe")
      .addEventListener(
        "click",
        (_: dom.Event) => {
          fullScreenHandler()
        },
        useCapture = false
      )
    jQuery.LoadingOverlay("show")
    val opts =
      new XHROptions[String](
        method = "GET",
        url = s"files/$jobID/$fileName",
        background = true,
        deserialize = deserializer
      )
    val reqPromise = m.request(opts)
    reqPromise.onSuccess {
      case data =>
        fullScreenHandler()
        m.render(document.getElementById(s"fileview_$resultName"), m.trust(data).asInstanceOf[VirtualDom.Child])
        jQuery.LoadingOverlay("hide")
    }
    reqPromise.onFailure[Throwable] {
      case e =>
        jQuery.LoadingOverlay("hide")
        println(s"Exception: ${e.getMessage}")
    }
    reqPromise.recover {
      case _ =>
        jQuery.LoadingOverlay("hide")
    }
  }

  private lazy val deserializer: UndefOr[js.Function1[String, String]] = js.defined((response: String) => response)

  private def fullScreenHandler(): Unit = {
    if (jQuery("#tool-tabs").hasClass("fullscreen")) {
      jQuery(".fileview").css("overflow-y", "auto")
      jQuery(".fileview").css("height", "100pc")
    } else { jQuery(".fileview").css("height", "30pc") }
  }

  private def downloadFile(jobID: String, fileName: String, resultName: String): Unit = {
    val filename = s"${resultName}_$jobID"
    val ending = resultName match {
      case "hhpred" | "hhomp" => ".hhr"
      case _                  => ".out"
    }
    val opts =
      new XHROptions[String](
        method = "GET",
        url = s"files/$jobID/$fileName",
        background = true,
        deserialize = deserializer
      )
    val reqPromise = m.request(opts)
    reqPromise.onSuccess {
      case data => DownloadHelper.download(filename + ending, data.asInstanceOf[String])
    }
    reqPromise.onFailure[Throwable] {
      case e =>
        if (e.getMessage contains "timeout")
          dom.window.alert("Request timeout: data might be too large.")
        else
          println(s"Exception: ${e.getMessage}")
    }
    reqPromise.recover {
      case _ =>
        jQuery.LoadingOverlay("hide")
    }
  }

}
