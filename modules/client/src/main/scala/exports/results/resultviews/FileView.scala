package exports.results.resultviews

import org.scalajs.jquery.{ jQuery, JQueryAjaxSettings, JQueryXHR }

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
    jQuery
      .ajax(
        js.Dictionary(
            "url"      -> s"files/$jobID/$fileName",
            "dataType" -> "text",
            "type"     -> "GET"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((data: js.Any, _: js.Any, _: JQueryXHR) => {
        jQuery(s"#fileview_$resultName").append(data.asInstanceOf[String])
      })
      .fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
        println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
      })
      .always(() => {
        jQuery.LoadingOverlay("hide")
      })
  }

  private def downloadFile(jobID: String, fileName: String, resultName: String): Unit = {
    val filename = s"${resultName}_$jobID"
    val ending = resultName match {
      case "hhpred" | "hhomp" => ".hhr"
      case _                  => ".out"
    }
    jQuery
      .ajax(
        js.Dictionary(
            "url"      -> s"files/$jobID/$fileName",
            "dataType" -> "text",
            "type"     -> "GET"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((data: js.Any, _: js.Any, _: JQueryXHR) => {
        DownloadHelper.download(filename + ending, data.asInstanceOf[String])
      })
      .fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
        println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")

      })
      .always(() => {
        jQuery.LoadingOverlay("hide")
      })
  }

}
