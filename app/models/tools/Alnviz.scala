package models.tools

import models.ToolModel
import play.api.data.Form
import play.api.data.Forms._


// TODO Dependency injection might come in handy here


/**
 * Singleton object that stores general information about a tool
 *
 * Created by lzimmermann on 14.12.15.
 */

object Alnviz extends ToolModel {
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


  val resultFileNames = Vector("result")

  // Specifies a finite set of values the parameter is allowed to assumepe
  val parameterValues = Map(
    "format" -> Set("fas", "clue", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}
case class Alnviz(alignment: String, format: String)