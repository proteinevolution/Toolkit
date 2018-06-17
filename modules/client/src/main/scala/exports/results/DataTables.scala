package exports.results

import org.scalajs.jquery.jQuery
import org.scalajs.dom.document

import scala.scalajs.js
import exports.facades.JQueryPlugin._

class DataTables(toolName: String) {

  private def lengthMenu(numHits: Int) = js.Array(
    js.Array(10, 25, 50, 100, numHits),
    js.Array("10", "25", "50", "100", "All")
  )

  def config(jobID: String, numHits: Int, callbacks: js.Function0[Unit]): Unit = {
    jQuery(document.getElementById("htb")).DataTable(
      js.Dictionary(
        "processing"   -> true,
        "serverSide"   -> true,
        "ajax"         -> s"/results/dataTable/$jobID",
        "autoWidth"    -> true,
        "lengthMenu"   -> lengthMenu(numHits),
        "searching"    -> true,
        "pageLength"   -> 25,
        "drawCallback" -> callbacks
      )
    )
  }

}
