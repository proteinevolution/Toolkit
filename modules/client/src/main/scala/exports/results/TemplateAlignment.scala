package exports.results

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.querki.jquery.{ $, JQueryAjaxSettings, JQueryXHR }
import org.scalajs.dom

@JSExportTopLevel("TemplateAlignment")
class TemplateAlignment(tool: String) {

  @JSExport
  def get(jobID: String, accession: String): Unit = {
    js.Dynamic.global.$.LoadingOverlay("show")
    $("textarea#alignmnentTemplate").value(" ")
    val acc = accession.replace("#", "%23")
    val extension = tool match {
      case "hhpred"  => "a3m"
      case "hhblits" => "ra3m"
      case "hhomp"   => "fas"
    }

    $("#alignmentTemplate").value("")

    $.ajax(
      js.Dynamic
        .literal(
          url = s"/results/templateAlignment/$jobID/$acc",
          success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
            $.ajax(
              js.Dynamic
                .literal(
                  url = s"/files/$jobID/$acc.$extension",
                  success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
                    $("#alignmentTemplate").value(data.toString)
                    js.Dynamic.global.$.LoadingOverlay("hide")
                  },
                  error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
                    dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
                    js.Dynamic.global.$.LoadingOverlay("hide")
                  },
                  `type` = "GET"
                )
                .asInstanceOf[JQueryAjaxSettings]
            )
          },
          error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
            dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
            js.Dynamic.global.$.LoadingOverlay("hide")
            $("#alignmentTemplate").value("Sorry, failed to fetch Template Alignment.")
          },
          `type` = "GET"
        )
        .asInstanceOf[JQueryAjaxSettings]
    )

  }
}
