package models

/**
 *
 * Defines the parameters of the tool
 *
 * Created by lzimmermann on 19.12.15.
 */
// Make sure that the case class parameter names and the keys in the map `inputForm` match
// Defines the fields of the Forms as Scala structure
case class AlnvizParam(sequence: String, format: String)