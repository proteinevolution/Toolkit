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

package exports.services

import org.scalajs.jquery.jQuery

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("AlertService")
object AlertService {

  @JSExport
  def alert(text: String, mode: String = "alert-success", timeout: Int = 4000): Unit = {
    jQuery("#alert-service-msg").addClass(mode).text(text).fadeIn("fast").delay(timeout).fadeOut("slow")
    import scala.scalajs.js.timers._
    setTimeout(timeout.toDouble + 500) {
      jQuery("#alert-service-msg").removeClass(mode)
    }
  }

}
