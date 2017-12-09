package de.proteinevolution.tel.param

trait Params {
  def generateValues(name: String): Map[String, String]
}
