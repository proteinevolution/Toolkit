package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.dom
import org.scalajs.dom.raw.Element
import scala.scalajs.js
import js.Dynamic.{ global => g }


@JSExportTopLevel("DataTables")
class DataTables(toolName: String) {

  private def lengthMenu(numHits: Int) = js.Array(
    js.Array(10, 25, 50, 100, numHits),
    js.Array("10", "25", "50", "100", "All")
  )

  private def callbacks = {
    if (toolName == "hhomp")
      js.undefined
    else {
      g.linkCheckboxes()
      //g.selectFromArray(g.checkboxes) // remove me from existence
    }
  }

  @JSExport
  def config(jobID: String, numHits: Int): Unit = {
    // TODO: monkey patch and callbacks
    js.Dynamic.global.$("#htb").dataTable(js.Dynamic.literal(
      "bProcessing" -> true,
      "bServerSide" -> true,
      "sAjaxSource" -> s"/results/dataTable/$jobID",
      "autoWidth" -> false,
      "lengthMenu" ->  lengthMenu(numHits),
      "searching" -> false,
      "iDisplayLength" -> 25,
      "drawCallback" -> callbacks.asInstanceOf[js.Any]
    ).asInstanceOf[js.Object])
  }

}
