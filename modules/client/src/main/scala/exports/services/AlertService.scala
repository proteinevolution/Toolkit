package exports.services

import org.scalajs.jquery.{ jQuery, JQuery }
import exports.facades.JQueryPlugin._
import org.scalajs.dom

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

/**
 * show alert like popup but uniform in style for all browsers
 */
@JSExportTopLevel("AlertService")
object AlertService {

  val $modal: JQuery         = jQuery("#alertModal")
  val $textContainer: JQuery = $modal.find(".alert-modal-text")

  @JSExport
  def alert(text: String): Unit = {
    if ($modal.length > 0 && $textContainer.length > 0) {
      $textContainer.text(text)
      $modal.foundation("open")
    } else { // fallback to js alert
      dom.window.alert(text)
    }
  }

}
