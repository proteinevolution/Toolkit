package models

import play.api.data.Form
import play.api.data.Forms._

/**
 * Singleton object that stores general information about a tool
 *
 * TODO Dependency injection might come in handy here
 *
 * Created by lzimmermann on 14.12.15.
 */

object Alnviz {

  val toolname = "alnviz"
  val fullName = "Alignment Visualizer"

  // Input Form Definition of this tool
  val inputForm = Form(
    mapping(
      "alignment" -> text,
      "format" -> text
    )(Alnviz.apply)(Alnviz.unapply)
  )

  //Map parameter identifier to the full names
  val parameterNames = Map(
    "alignment" -> "Sequence Alignment",
    "format"    -> "Alignment Format")

  // Specifies a finite set of values the parameter is allowed to assume
  val parameterValues = Map(
    "format" -> Set("fas", "clue", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )

  // Specify which arguments need to go to a file for further processing
  val files = List("alignment")
}
case class Alnviz(sequence: String, format: String)
