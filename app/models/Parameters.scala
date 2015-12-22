package models

/**
 *
 * Defines the parameters the tools.
 *
 * Created by lzimmermann on 19.12.15.
 */
// Make sure that the case class parameter names and the keys in the map `inputForm` match
// Defines the fields of the Forms as Scala structure

object Parameters {

  case class Alnviz(sequence: String, format: String)
  case class TCoffee(sequences: String)
}