package exports.services

import exports.facades.JQueryPlugin._
import org.scalajs.jquery.{JQuery, jQuery}

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("ConfirmService")
object ConfirmService {

  val $modal: JQuery = jQuery("#confirmModal")

  @JSExport
  def confirm(text: String, onSuccess: js.Function0[Unit], onError: js.Function0[Unit] = () => {}): Unit = {
    $modal.find(".modal-text").text(text)
    $modal.foundation("open")
    $modal.find(".confirm-btn").off("click").on("click", () => {
      $modal.foundation("close")
      onSuccess()
    })
    $modal.find(".cancel-btn").off("click").on("click", () => {
      $modal.foundation("close")
      onError()
    })
  }
}
