package exports.results

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.dom
import org.scalajs.jquery._

@JSExportTopLevel("TemplateAlignment")
class TemplateAlignment(tool: String) {

  @JSExport
  def get(jobID: String, accession: String): Unit = {
    jQuery.asInstanceOf[exports.facades.JQuery].LoadingOverlay("show")
    jQuery("textarea#alignmnentTemplate").value(" ")
    val acc = accession.replace("#", "%23")
    val extension = tool match {
      case "hhpred"  => "a3m"
      case "hhblits" => "ra3m"
      case "hhomp"   => "fas"
    }

    jQuery("#alignmentTemplate").value("")

    jQuery.ajax(
      js.Dictionary(
          "url" -> s"/results/templateAlignment/$jobID/$acc",
          "success" -> { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
            jQuery.ajax(
              js.Dictionary(
                  "url" -> s"/files/$jobID/$acc.$extension",
                  "success" -> { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
                    jQuery("#alignmentTemplate").value(data.toString)
                    jQuery.asInstanceOf[exports.facades.JQuery].LoadingOverlay("hide")
                  },
                  "error" -> { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
                    dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
                    jQuery.asInstanceOf[exports.facades.JQuery].LoadingOverlay("hide")
                  }
                )
                .asInstanceOf[JQueryAjaxSettings]
            )
          },
          "error" -> { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
            dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
            jQuery.asInstanceOf[exports.facades.JQuery].LoadingOverlay("hide")
            jQuery("#alignmentTemplate").value("Sorry, failed to fetch Template Alignment.")
          }
        )
        .asInstanceOf[JQueryAjaxSettings]
    )

  }
}
