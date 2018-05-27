package exports.results.resultviews

import org.scalajs.jquery.jQuery
import com.tgf.pizza.scalajs.mithril._

import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.document

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import exports.facades.JQueryPlugin._
import exports.results.DownloadHelper

@JSExportTopLevel("FileView")
class FileView() {

  @JSExport("apply")
  def apply(jobID: String, fileName: String, resultName: String): Unit = {
    document
      .getElementById("download_alignment")
      .addEventListener("click", (_: dom.Event) => {
        downloadFile(jobID, fileName, resultName)
      }, useCapture = false)
    jQuery.LoadingOverlay("show")
    val opts =
      new XHROptions[String](
        method = "GET",
        dataType = "text",
        url = s"files/$jobID/$fileName",
        background = true
      )
    val reqPromise = m.request(opts)
    reqPromise.onSuccess {
      case data =>
        println(data.asInstanceOf[String]) //jQuery(s"#fileview_$resultName").append(data.asInstanceOf[String])
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

  private def downloadFile(jobID: String, fileName: String, resultName: String): Unit = {
    val filename = s"${resultName}_$jobID"
    val ending = resultName match {
      case "hhpred" | "hhomp" => ".hhr"
      case _                  => ".out"
    }
    val opts =
      new XHROptions[js.Object](
        method = "GET",
        dataType = "text",
        url = s"files/$jobID/$fileName",
        background = true
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
