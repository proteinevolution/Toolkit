package models.tools

import play.api.data.Form
import play.api.data.Forms._


// TODO Dependency injection might come in handy here

/**
  * Singleton object that stores general information about a tool
  */
object Mafft extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "mafft"
  val toolNameLong:String         = "Mafft"
  val toolNameAbbreviation:String = "mft"


  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "sequences" -> nonEmptyText,
      "gapopen" -> bigDecimal(5,3),
      "offset" -> bigDecimal(5,3)
    )
  )
}