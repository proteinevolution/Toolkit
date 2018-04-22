package exports.facades

import scala.scalajs.js
import scala.scalajs.js.annotation.ScalaJSDefined

@ScalaJSDefined
trait ResultContext extends js.Object {

  def toolName: String

  def numHits: Int

  def query: SingleSeq

  def belowEvalThreshold: Int

  def firstQueryStart: Int

  def firstQueryEnd: Int

}