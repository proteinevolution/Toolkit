package models

import play.api.data.Form
import play.api.data.Forms._


// TODO Dependency injection might come in handy here


/**
 * Singleton object that stores general information about a tool
 *
 * Created by lzimmermann on 14.12.15.
 */

object Alnviz {

  val toolname = "alnviz"
  val fullName = "Alignment Visualizer"


  // Parameter List
  val parameters = Vector(FileParam("alignment", None), StringParam("format", None))


  // Input Form Definition of this tool
  val inputForm = Form(
    mapping(
      "alignment" -> text,
      "format" -> text
    )(Alnviz.apply)(Alnviz.unapply)
  )


  val exec  = Vector("perl",  "reformat.pl", "-i", parameters(1), "-o", "clu", "-f", parameters(0), "-a", "result")


  //Map parameter identifier to the full names
  val parameterNames = Map(
    "alignment" -> "Sequence Alignment",
    "format"    -> "Alignment Format")

  // Specifies a finite set of values the parameter is allowed to assume
  val parameterValues = Map(
    "format" -> Set("fas", "clue", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )


}
case class Alnviz(sequence: String, format: String)
