package exports.results

import java.util.UUID

import com.tgf.pizza.scalajs.mithril._
import org.scalajs.dom
import org.scalajs.jquery.{ jQuery, JQueryAjaxSettings, JQueryXHR }

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import scala.scalajs.js.timers._

@JSExportTopLevel("Forwarding")
object Forwarding {

  import js.Dynamic.{ global => g }

  @JSExport
  def process(selectedTool: String, hasEvalue: Boolean, evalue: String, isFullLength: Boolean): Unit = {
    g.$.LoadingOverlay("show")
    val checkboxes = ResultViewHelper.getSelectedValues
    if (checkboxes.length < 1 && !hasEvalue) {
      g.$(".forwardModal").foundation("close")
      g.$.LoadingOverlay("hide")
      g.alert("No sequence(s) selected!")
      return
    }
    val filename  = UUID.randomUUID().toString.toUpperCase
    val baseRoute = "/results/forwardAlignment/" + g.jobID
    val route = (hasEvalue, isFullLength) match {
      case (true, true)   => s"$baseRoute/evalFull"
      case (false, true)  => s"$baseRoute/full"
      case (true, false)  => s"$baseRoute/alnEval"
      case (false, false) => s"$baseRoute/aln"
    }
    jQuery.ajax(
      js.Dynamic
        .literal(
          url = route,
          data = JSON.stringify(
            js.Dynamic.literal("fileName" -> filename, "evalue" -> evalue, "checkboxes" -> checkboxes)
          ),
          contentType = "application/json",
          success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
            redirect(selectedTool, s"files/${g.jobID}/$filename.fa")
          },
          error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
            dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
            g.$.LoadingOverlay("hide")
          },
          `type` = "POST"
        )
        .asInstanceOf[JQueryAjaxSettings]
    )
  }

  @JSExport
  def redirect(tool: String, forwardPath: String): Unit = {
    m.route(s"/tools/$tool")
    jQuery.ajax(
      js.Dynamic
        .literal(
          url = forwardPath,
          success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
            if (tool == "reformat") {
              setTimeout(100) { () =>
                g.myCodeMirror.setValue(data.asInstanceOf[js.Array[String]])
                g.$.LoadingOverlay("hide")
              }
            } else {
              jQuery("#alignment").value(data.asInstanceOf[js.Array[String]])
              g.$.LoadingOverlay("hide")
              g.validationProcess(jQuery("#alignment"), jQuery("#toolnameAccess").value())
            }
          },
          error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
            dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
            g.$.LoadingOverlay("hide")
          },
          `type` = "GET"
        )
        .asInstanceOf[JQueryAjaxSettings]
    )
  }

  @JSExport
  def simple(tool: String, forwardData: String): Unit = {
    if (forwardData.isEmpty) {
      g.alert("No sequence(s) selected!")
      g.$.LoadingOverlay("hide")
      return
    }
    try {
      dom.window.localStorage.setItem("resultcookie", forwardData)
    } catch {
      case e: Throwable =>
        g.alert(e.getLocalizedMessage)
        e.printStackTrace()
        throw e
    }
    m.route(s"/tools/$tool")
  }

}
