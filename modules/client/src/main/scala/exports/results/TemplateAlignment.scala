package exports.results

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.querki.jquery.{ $, JQueryAjaxSettings, JQueryXHR }
import org.scalajs.dom

@JSExportTopLevel("TemplateAlignment")
object TemplateAlignment {

  @JSExport
  def get(jobID: String, accession: String): Unit = {
    js.Dynamic.global.$.LoadingOverlay("show")
    $("textarea#alignmnentTemplate").value(" ")
    val acc = accession.replace("#", "%23")

    $.ajax(
      js.Dynamic
        .literal(
          url = s"/results/templateAlignment/$jobID/$acc",
          success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
            $.ajax(
              js.Dynamic
                .literal(
                  url = s"/files/$jobID /$acc.fas",
                  success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
                    $("#alignmentTemplatehhomp").value(data.toString)
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
            $("#alignmentTemplatehhomp").value("Sorry, failed to fetch Template Alignment.")
          },
          `type` = "GET"
        )
        .asInstanceOf[JQueryAjaxSettings]
    )

  }
}
