package models.tools

import play.api.data.Form
import play.api.data.Forms._


// TODO Dependency injection might come in handy here

/**
  * Singleton object that stores general information about a tool
  */
object Clans extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "clans"
  val toolNameLong:String         = "Clans"
  val toolNameAbbreviation:String = "clns"


  // --- Clans specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "standarddb" -> text,
      "psiblastmode" -> boolean,
      "protblastmode" -> boolean,
      "firstevalue" -> number,
      "complexityfilter" -> boolean,
      "ungapped" -> boolean,
      "customid" -> text
    )
  )
  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}