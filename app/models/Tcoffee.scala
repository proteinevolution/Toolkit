package models

/**
 * Created by snam on 21.12.15.
 */
object Tcoffee {

  val fullName = "TCoffee"

  //Map parameter identifier to the full names
  val parameterNames = Map(
    "alignment" -> "Sequence Alignment",
    "format"    -> "Alignment Format")


  // Specifies a finite set of values the parameter is allowed to assume
  val parameterValues = Map(
    "format" -> Set("fas", "clue", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}
