package models.tools

import play.api.data.Form
import play.api.data.Forms._

/**
  * Singleton object that stores general information about a tool
  */
object Reformat extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "reformat"
  val toolNameLong:String         = "Reformat"
  val toolNameAbbreviation:String = "form"

  // --- Hmmer3 specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text
    )
  )
}