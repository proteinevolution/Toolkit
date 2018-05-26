package exports.services

import org.scalajs.jquery.jQuery

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

/**
 * show alert like popup but uniform in style for all browsers
 */
@JSExportTopLevel("AlertService")
object AlertService {

  @JSExport
  def alert(text: String, mode: String = ""): Unit = {
    jQuery("#alert-service-msg")
        .text(text)
        .addClass(mode)
        .fadeIn("fast")
        .delay(4000)
        .fadeOut("slow")
        .removeClass(mode)
  }

}
