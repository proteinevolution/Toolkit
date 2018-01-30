package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.dom
import org.scalajs.dom.raw.{ HTMLInputElement, NodeListOf }


/**
 * Checkbox handler for result pages
 */

@JSExportTopLevel("Checkboxes")
object Checkboxes {

  @JSExport
  def link() = {

    ???
  }

  @JSExport
  def check(): Unit = {
    val checkboxes = dom.document.querySelectorAll("input[type=checkbox]:checked").asInstanceOf[NodeListOf[HTMLInputElement]]
    dom.console.log(checkboxes.length)
  }

}
