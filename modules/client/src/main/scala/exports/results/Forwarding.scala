package exports.results

import java.util.UUID

import org.querki.jquery.{ $, JQueryAjaxSettings, JQueryXHR }
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import scala.scalajs.js.timers._

import com.tgf.pizza.scalajs.mithril._

@JSExportTopLevel("Forwarding")
object Forwarding {

  import js.Dynamic.{ global => g }

  @JSExport
  def process(selectedTool: String, hasEvalue: Boolean, evalue: String, isFullLength: Boolean): Unit = {
    g.$.LoadingOverlay("show")
    val checkboxes = g.checkboxes.asInstanceOf[js.Array[Int]].distinct
    if (checkboxes.length < 1) {
      g.$(".forwardModal").foundation("close")
      g.$.LoadingOverlay("hide")
      g.alert("No sequence(s) selected!")
      return
    }
    val filename = UUID.randomUUID().toString.toUpperCase
    val route = (hasEvalue, isFullLength) match {
      case (true, true)   => "/results/forwardAlignment/" + g.jobID + "/evalFull"
      case (false, true)  => "/results/forwardAlignment/" + g.jobID + "/evalFull"
      case (true, false)  => "/results/forwardAlignment/" + g.jobID + "/alnEval"
      case (false, false) => "/results/forwardAlignment/" + g.jobID + "/aln"
    }
    $.ajax(
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
    $.ajax(
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
              $("#alignment").value(data.asInstanceOf[js.Array[String]])
              g.$.LoadingOverlay("hide")
              g.validationProcess($("#alignment"), $("#toolnameAccess").value())
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
