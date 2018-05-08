package exports.results

import scala.scalajs.js

class DataTables(toolName: String) {

  private def lengthMenu(numHits: Int) = js.Array(
    js.Array(10, 25, 50, 100, numHits),
    js.Array("10", "25", "50", "100", "All")
  )

  def config(jobID: String, numHits: Int, callbacks: () => js.Any): Unit = {
    js.Dynamic.global
      .$("#htb")
      .DataTable(
        js.Dynamic
          .literal(
            "processing"   -> true,
            "serverSide"   -> true,
            "ajax"         -> s"/results/dataTable/$jobID",
            "autoWidth"    -> false,
            "lengthMenu"   -> lengthMenu(numHits),
            "searching"    -> true,
            "pageLength"   -> 25,
            "drawCallback" -> callbacks
          )
          .asInstanceOf[js.Object]
      )
  }

}
