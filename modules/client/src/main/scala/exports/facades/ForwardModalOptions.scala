package exports.facades

import scala.scalajs.js

trait ForwardModalOptions extends js.Object {

  def heading: String

  def showControlArea: Boolean

  def showRadioBtnSelection: Boolean

  def showRadioBtnSequenceLength: Boolean

  def alignmentOptions: js.Array[String]

  def multiSeqOptions: js.Array[String]

}
