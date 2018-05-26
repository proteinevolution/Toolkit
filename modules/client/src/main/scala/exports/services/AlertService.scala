package exports.services

import org.scalajs.jquery.jQuery

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

/**
 * show alert like popup but uniform in style for all browsers
 */
@JSExportTopLevel("AlertService")
object AlertService {

  @JSExport
  def alert(text: String, mode: String = "alert-danger"): Unit = {
    jQuery("#alert-service-msg")
      .addClass(mode)
      .text(text)
      .fadeIn("fast")
      .delay(4000)
      .fadeOut("slow")
    import scala.scalajs.js.timers._
    setTimeout(4500) {
      jQuery("#alert-service-msg")
        .removeClass(mode)
    }
  }

}
