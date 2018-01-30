package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.dom


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
    val checkboxes = dom.document.querySelector("input:checkbox")
    checkboxes.addEventListener(
      "change", { (event: dom.Event) =>
        event.preventDefault()
        dom.console.log("clicked")
      },
      false
    )
  }

}
