/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package exports.results

import java.util.UUID

import com.tgf.pizza.scalajs.mithril._
import exports.facades.JQueryPlugin._
import exports.results.models.ForwardingForm.{ ForwardingFormAln, ForwardingFormNormal }
import exports.services.AlertService
import org.scalajs.dom
import org.scalajs.jquery.{ jQuery, JQueryAjaxSettings, JQueryXHR }
import upickle.default.write

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{ global => g }
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import scala.scalajs.js.timers._

@JSExportTopLevel("Forwarding")
object Forwarding {

  def processResults(
      jobID: String,
      selectedTool: String,
      hasEvalue: Boolean,
      evalue: String,
      isFullLength: Boolean
  ): Unit = {
    val checkboxes = g.Toolkit.resultView.getSelectedValues.asInstanceOf[js.Array[Int]]

    if (checkboxes.length < 1 && !hasEvalue) {
      jQuery(".forwardModal").foundation("close")
      AlertService.alert("No sequences selected!", "alert-danger")
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
    val opts =
      new XHROptions[js.Object](
        method = "POST",
        url = route,
        data = JSON
          .parse(write(ForwardingFormNormal(filename, evalue, checkboxes.toArray)))
          .asInstanceOf[js.UndefOr[js.Object]],
        background = true
      )
    val reqPromise = m.request(opts)
    reqPromise.onSuccess {
      case _ => redirect(selectedTool, s"results/files/$jobID/$filename.fa")
    }
    reqPromise.onFailure[Throwable] {
      case e =>
        if (e.getMessage contains "timeout")
          AlertService.alert("Request timeout: the forwarded data might be too large.", "alert-danger")
        else
          println(s"Exception: ${e.getMessage}")
    }
    reqPromise.recover {
      case _ =>
        jQuery.LoadingOverlay("hide")
    }
  }

  def processAlnResults(jobID: String, selectedTool: String, resultName: String): Unit = {
    val checkboxes = js.Dynamic.global.Toolkit.resultView.getSelectedValues.asInstanceOf[js.Array[Int]]
    if (checkboxes.length < 1) {
      jQuery(".forwardModal").foundation("close")
      AlertService.alert("No sequences selected!", "alert-danger")
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

  def redirect(tool: String, forwardPath: String): Unit = {
    jQuery
      .ajax(
        js.Dictionary(
            "url"    -> forwardPath,
            "method" -> "GET"
          )
          .asInstanceOf[JQueryAjaxSettings]
      )
      .done((data: js.Any, _: js.Any, _: JQueryXHR) => {
        simple(tool, data.toString)
      })
      .fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
        println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
      })
      .always(() => {
        jQuery.LoadingOverlay("hide")
      })
  }

  @JSExport
  def simple(tool: String, forwardData: String, jobID: String = ""): Unit = {
    if (forwardData.isEmpty) {
      AlertService.alert("No sequences selected!", "alert-danger")
      return
    }
    m.route(s"/tools/$tool")
    tryPasting(tool, forwardData, jobID)
  }

  def tryPasting(tool: String, forwardData: String, jobID: String): Unit = {
    if (dom.window.location.hash != s"#/tools/$tool") {
      setTimeout(10) {
        tryPasting(tool, forwardData, jobID)
      }
    } else if (tool == "reformat") {
      setTimeout(500) { // there is no way to determine whether reformat is loaded. Because it is not used a lot, we just use a bigger timeout to make sure.
        g.myCodeMirror.setValue(forwardData)
      }
    } else {
      jQuery("#alignment").value(forwardData)
      jQuery("#parent_id").value(jobID)
      g.validationProcess(jQuery("#alignment"), jQuery("#toolnameAccess").value())
    }
  }

}
