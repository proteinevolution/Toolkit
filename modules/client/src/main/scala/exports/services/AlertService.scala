package exports.services

import org.scalajs.jquery.jQuery

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("AlertService")
object AlertService {

  @JSExport
  def alert(
      text: String,
      mode: String = "alert-success",
      timeout: Int = 4000
  ): Unit = {
    jQuery("#alert-service-msg")
      .addClass(mode)
      .text(text)
      .fadeIn("fast")
      .delay(timeout)
      .fadeOut("slow")
    import scala.scalajs.js.timers._
    setTimeout(timeout.toDouble + 500) {
      jQuery("#alert-service-msg").removeClass(mode)
    }
  }

}
