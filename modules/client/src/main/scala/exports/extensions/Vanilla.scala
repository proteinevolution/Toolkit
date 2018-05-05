package exports.extensions

import org.scalajs.dom.raw.HTMLLinkElement

import scala.scalajs.js

object Vanilla {

  @js.native
  sealed trait Navigator extends js.Object {
    def msSaveBlob(blob: js.Any, filename: String): Unit = js.native
    def msSaveOrOpenBlob: Boolean                        = js.native
  }

  @js.native
  sealed trait AugmentedWindowURL extends js.Object {
    def createObjectURL(blob: js.Any): String = js.native
    def revokeObjectURL(href: String): Unit   = js.native
  }

  @js.native
  trait ExtendedWindow extends js.Object {
    var URL: AugmentedWindowURL = js.native
    var navigator: Navigator    = js.native
  }

  @js.native
  trait ExtendedLink extends HTMLLinkElement {
    var download: String = js.native
    def remove(): Unit   = js.native
  }

}
