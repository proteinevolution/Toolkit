package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.scalajs.dom
import org.scalajs.dom.html
import org.scalajs.dom.raw.NodeListOf

import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Checkbox handler for result pages
 */

@JSExportTopLevel("Checkboxes")
object Checkboxes {

  @JSExport
  def getChecked: js.Array[String] = {
    val checkboxes = dom.document.querySelectorAll("input[type=checkbox]:checked").asInstanceOf[NodeListOf[html.Input]]
    val x = (0 to checkboxes.length).map { i =>
      dom.console.log(i)
      dom.console.log(checkboxes(i).value)
      checkboxes(i).value
    }.distinct.toJSArray
    dom.console.log(x)
    x
  }

}
