package models.tools

import play.api.data.Form
import play.api.data.Forms._

// TODO Dependency injection might come in handy here

/**
  * Singleton object that stores general information about a tool
  */
object Hmmer3 extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "hmmer3"
  val toolNameLong:String         = "Hmmer3"
  val toolNameAbbreviation:String = "hm3"

  // --- Hmmer3 specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text
    )
  )
}