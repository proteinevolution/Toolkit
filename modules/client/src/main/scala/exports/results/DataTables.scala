package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.dom
import org.scalajs.dom.raw.Element

import scala.scalajs.js

@JSExportTopLevel("DataTables")
object DataTables {

  private val htb: Element = dom.document.getElementById("htb")

  @JSExport
  def config(jobID: String, numHits: Int): Unit = {
    htb.asInstanceOf[js.Dynamic].dataTable(js.Dynamic.literal(
      "bProcessing" -> true,
      "bServerSide" -> true,
      "sAjaxSource" -> s"/results/dataTable/$jobID",
      "autoWidth" -> false,
      "lengthMenu" ->  Array(Array(10, 25, 50, 100, numHits)).asInstanceOf[js.Array[Int]],
      "searching" -> false,
      "iDisplayLength" -> 25
    ))
  }

}
