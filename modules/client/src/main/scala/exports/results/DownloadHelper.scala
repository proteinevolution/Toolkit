package exports.results

import exports.extensions.Vanilla.{ ExtendedLink, ExtendedWindow }
import org.scalajs.dom
import org.scalajs.dom.raw.{ Blob, BlobPropertyBag }

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("DownloadHelper")
object DownloadHelper {

  @JSExport
  def download(filename: String, text: String) {
    val blob      = new Blob(js.Array(text), BlobPropertyBag("application/octet-stream"))
    val extWindow = dom.window.asInstanceOf[ExtendedWindow]
    if (extWindow.navigator.msSaveOrOpenBlob.toOption.getOrElse(false)) {
      extWindow.navigator.msSaveBlob(blob, filename)
    } else {
      val a = dom.document.createElement("a").asInstanceOf[ExtendedLink]
      a.href = extWindow.URL.createObjectURL(blob)
      a.download = filename
      // Append anchor to body.
      dom.document.body.appendChild(a)
      a.click()
      extWindow.URL.revokeObjectURL(a.href)
      // Remove anchor from body
      a.remove()
    }
    js.Dynamic.global.$.LoadingOverlay("hide")
  }

}
