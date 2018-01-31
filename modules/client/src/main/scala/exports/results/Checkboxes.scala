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
  def getChecked(): js.Array[Int] = {
    val checkboxes = dom.document.querySelectorAll("input[type=checkbox]:checked").asInstanceOf[NodeListOf[html.Input]]
    val x = for {i <- (0 to checkboxes.length - 1)}
    yield checkboxes(i).value.toInt
    x.distinct.toJSArray
  }

}
