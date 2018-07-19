package exports.facades

import org.scalajs.dom
import org.scalajs.dom.raw.Blob

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@JSGlobal
@js.native
object NGL extends js.Object {

  @js.native
  class Stage(
      element: dom.Element,
      settings: js.Dictionary[Any] = js.Dictionary()
  ) extends js.Any {

    def loadFile(url: Blob, options: js.Dictionary[Any]): Unit = js.native

    def loadFile(url: String, options: js.Dictionary[Any]): Unit = js.native

    def dispose(): Unit = js.native

  }

}
