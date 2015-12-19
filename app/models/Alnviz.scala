package models

/**
 * Singleton object that stores general information about a tool
 *
 * TODO Dependency injection might come in handy here
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
    "format" -> Set("fas", "clue", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}
