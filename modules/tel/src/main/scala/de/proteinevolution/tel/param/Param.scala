package de.proteinevolution.tel.param

import de.proteinevolution.tel.TELRegex
import de.proteinevolution.tel.env.EnvAware

import scala.collection.immutable.ListMap

abstract class Param(val name: String) extends EnvAware[Param] with TELRegex

abstract class PredicativeParam(name: String) extends Param(name) {

  /**
   * Decides whether or not the value for this parameter is allowed
   * @param value The value that should be tested for its validity
   * @return Whether the provided value is valid for this parameter
   */
  def validate(value: String): Boolean
}

abstract class GenerativeParam(name: String) extends PredicativeParam(name) {

  // Sequence of allowed values with respective clear text name
  def validate(value: String): Boolean = this.generate.contains(value)

  def generate: ListMap[String, String]

}
