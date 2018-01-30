package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.NodeListOf


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
    val checkboxes = dom.document.querySelectorAll("input[type=checkbox]:checked").asInstanceOf[NodeListOf[html.Input]]

    for (i <- checkboxes.length) {
      dom.console.log(checkboxes(i).value)
    }
    
  }

}
