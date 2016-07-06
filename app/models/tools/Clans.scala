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
}