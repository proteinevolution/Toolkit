package exports.results

import java.util.UUID

import org.querki.jquery.{ $, JQueryAjaxSettings, JQueryXHR }
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("Forwarding")
object Forwarding {

  import js.Dynamic.{ global => g }

  @JSExport
  def process(selectedTool: String,
              boolSelectedHits: Boolean, // what is this for? globally not used
              boolEvalue: Boolean,
              evalue: String,
              boolFullLength: Boolean): Unit = {

    g.$.LoadingOverlay("show")
    val checkboxes = g.checkboxes.removeDuplicates().asInstanceOf[js.Array[Int]]

    if (checkboxes.length < 1) {
      g.$(".forwardModal").foundation("close")
      g.$.LoadingOverlay("hide")
      g.alert("No sequence(s) selected!")
      return
    }

    val filename = UUID.randomUUID().toString.toUpperCase
    val route = (boolSelectedHits, boolEvalue, boolFullLength) match {

      case (_, true, true)   => "/results/forwardAlignment/" + g.jobID + "/evalFull"
      case (_, false, true)  => "/results/forwardAlignment/" + g.jobID + "/evalFull"
      case (_, true, false)  => "/results/forwardAlignment/" + g.jobID + "/alnEval"
      case (_, false, false) => "/results/forwardAlignment/" + g.jobID + "/aln"
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
            g.forwardPath(selectedTool, s"files/${g.jobID}/$filename.fa")
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

}
