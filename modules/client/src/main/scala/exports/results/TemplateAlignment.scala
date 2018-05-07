package exports.results

import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("TemplateAlignment")
class TemplateAlignment(tool: String) {

  @JSExport
  def get(jobID: String, accession: String): Unit = {
    val textArea = jQuery("textarea.alignmentTemplateTextArea")
    textArea.asInstanceOf[exports.facades.JQuery].LoadingOverlay("show")
    val acc = accession.replace("#", "%23")
    val extension = tool match {
      case "hhpred" => "a3m"
      case "hhblits" => "ra3m"
      case "hhomp" => "fas"
    }

    jQuery.ajax(js.Dictionary(
      "url" -> s"/results/templateAlignment/$jobID/$acc"
    ).asInstanceOf[JQueryAjaxSettings]
    ).done((_: js.Any, _: js.Any, _: JQueryXHR) => {
      jQuery.ajax(js.Dictionary(
        "url" -> s"/files/$jobID/$acc.$extension"
      ).asInstanceOf[JQueryAjaxSettings]
      ).done((data: js.Any, _: js.Any, _: JQueryXHR) => {
        textArea.value(data.toString)
      }).fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
        println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
        textArea.value("Sorry, failed to fetch Template Alignment.")
      }).always(() => {
        textArea.asInstanceOf[exports.facades.JQuery].LoadingOverlay("hide")
      })
    }).fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
      println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
      textArea.value("Sorry, failed to fetch Template Alignment.")
    }).always(() => {
      textArea.asInstanceOf[exports.facades.JQuery].LoadingOverlay("hide")
    })
  }
}
