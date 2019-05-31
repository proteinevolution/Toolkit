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
