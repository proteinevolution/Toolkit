package models

/**
 *
 *
 * Created by lzimmermann on 14.12.15.
 */

object Alnviz {

  val fullName = "Alignment Visualizer"

  //Map parameter identifier to the full names
  val parameterNames = Map(
    "alignment" -> "Sequence Alignment",
    "format"    -> "Alignment Format")


  // Specifies a finite set of values the parameter is allowed to assume
  val parameterValues = Map(
    "format" -> List("fas", "clue", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}
