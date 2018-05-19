package exports.facades

import org.scalajs.dom.raw.Blob

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

@JSGlobal
@js.native
object NGL extends js.Object {

  @js.native
  class Stage(elementId: String, settings: js.Dictionary[Any] = js.Dictionary()) extends js.Any {
    def loadFile(url: Blob, options: js.Dictionary[Any]): js.native = js.native

    def loadFile(url: String, options: js.Dictionary[Any]): js.native = js.native

    def dispose(): js.native = js.native
  }

}
