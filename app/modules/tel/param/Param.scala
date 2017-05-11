package modules.tel.param

import modules.tel.TELRegex
import modules.tel.env.EnvAware

/**
  * Created by lzimmermann on 10/11/16.
  */
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
  def generate: Map[String, String]
}
