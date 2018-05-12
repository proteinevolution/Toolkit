package exports.results

import java.util.UUID

import com.tgf.pizza.scalajs.mithril._
import exports.facades.JQueryPlugin._
import exports.results.models.ForwardingForm.{ ForwardingFormAln, ForwardingFormNormal }
import org.scalajs.dom
import org.scalajs.jquery.{ jQuery, JQueryAjaxSettings, JQueryXHR }
import upickle.default.write

import scala.scalajs.js
import scala.scalajs.js.timers._

object Forwarding {

  import js.Dynamic.{ global => g }

  def processResults(jobID: String,
                     selectedTool: String,
                     hasEvalue: Boolean,
                     evalue: String,
                     isFullLength: Boolean): Unit = {
    val checkboxes = g.Toolkit.resultView.getSelectedValues.asInstanceOf[js.Array[Int]]

    if (checkboxes.length < 1 && !hasEvalue) {
      jQuery(".forwardModal").foundation("close")
      dom.window.alert("No sequence(s) selected!")
      return
    }

    val filename  = UUID.randomUUID().toString.toUpperCase
    val baseRoute = "/results/forwardAlignment/" + jobID
    val route = (hasEvalue, isFullLength) match {
      case (true, true)   => s"$baseRoute/evalFull"
      case (false, true)  => s"$baseRoute/full"
      case (true, false)  => s"$baseRoute/alnEval"
      case (false, false) => s"$baseRoute/aln"
    }
    jQuery.LoadingOverlay("show")
    jQuery
      .ajax(
        js.Dictionary(
            "url"         -> route,
            "data"        -> write(ForwardingFormNormal(filename, evalue, checkboxes.toArray)),
            "contentType" -> "application/json",
            "method"      -> "POST"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((_: js.Any, _: js.Any, jqXHR: JQueryXHR) => {
        if (jqXHR.status == 204) {
          redirect(selectedTool, s"files/$jobID/$filename.fa")
        }
      })
      .fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
        println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
        jQuery.LoadingOverlay("hide")
      })
  }

  def processAlnResults(jobID: String, selectedTool: String, resultName: String): Unit = {
    val checkboxes = js.Dynamic.global.Toolkit.resultView.getSelectedValues.asInstanceOf[js.Array[Int]]
    if (checkboxes.length < 1) {
      jQuery(".forwardModal").foundation("close")
      dom.window.alert("No sequence(s) selected!")
      return
    }
    jQuery.LoadingOverlay("show")
    jQuery
      .ajax(
        js.Dictionary(
            "url"         -> s"/results/alignment/getAln/$jobID",
            "data"        -> write(ForwardingFormAln(resultName, checkboxes.toArray)),
            "contentType" -> "application/json",
            "method"      -> "POST"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((data: js.Any, _: js.Any, _: JQueryXHR) => {
        simple(selectedTool, data.toString)
      })
      .fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
        println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
      })
      .always(() => {
        jQuery.LoadingOverlay("hide")
      })
  }

  def processFiles(selectedTool: String, fileUrl: String): Unit = {
    jQuery.LoadingOverlay("show")
    jQuery
      .ajax(
        js.Dictionary(
            "method"   -> "GET",
            "url"      -> fileUrl,
            "dataType" -> "text"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((data: js.Object) => {
        simple(selectedTool, data.toString)
      })
      .always(() => {
        jQuery.LoadingOverlay("hide")
      })
  }

  def redirect(tool: String, forwardPath: String): Unit = {
    m.route(s"/tools/$tool")
    jQuery
      .ajax(
        js.Dictionary(
            "url"    -> forwardPath,
            "method" -> "GET"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((data: js.Any, _: js.Any, _: JQueryXHR) => {
        if (tool == "reformat") {
          setTimeout(100) { () =>
            g.myCodeMirror.setValue(data.asInstanceOf[js.Array[String]])
          }
        } else {
          jQuery("#alignment").value(data.asInstanceOf[js.Array[String]])
          g.validationProcess(jQuery("#alignment"), jQuery("#toolnameAccess").value())
        }
      })
      .fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
        println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
      })
      .always(() => {
        jQuery.LoadingOverlay("hide")
      })
  }

  def simple(tool: String, forwardData: String): Unit = {
    if (forwardData.isEmpty) {
      dom.window.alert("No sequence(s) selected!")
      return
    }
    try {
      dom.window.localStorage.setItem("resultcookie", forwardData)
    } catch {
      case e: Throwable =>
        dom.window.alert(e.getLocalizedMessage)
        e.printStackTrace()
        throw e
    }
    m.route(s"/tools/$tool")
  }

}
