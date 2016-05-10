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


}