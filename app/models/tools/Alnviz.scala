package models.tools

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
  val fullName = "AlignmentViewer"

  // --- Names for the Tool ---
  val toolNameShort: String = "alnviz"
  val toolNameLong: String = "Alnviz"
  val toolNameAbbreviation: String = "avz"

  // --- Alnviz specific values ---
  // Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text
    )
  )

  val resultFileNames = Vector("result")

  // Specifies a finite set of values the parameter is allowed to assumepe
  val parameterValues = Map(
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}
