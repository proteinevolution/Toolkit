package models

/**
 *
 *
 * Created by snam on 21.12.15.
 */
object Tcoffee {

  val fullName = "TCoffee"

  //Map parameter identifier to the full names
  val parameterNames = Map(
    "sequences" -> "Sequences to be aligned")
}
case class Tcoffee(sequences: String)
