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


  // --- Clansspecific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "sequences" -> nonEmptyText,
      "gapopen" -> bigDecimal(5,3),
      "offset" -> bigDecimal(5,3)
    )
  )
  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}