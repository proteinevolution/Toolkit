package models.tools

import play.api.data.Form
import play.api.data.Forms._


// TODO Dependency injection might come in handy here

/**
  * Singleton object that stores general information about a tool
  */
object Tcoffee extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "tcoffee"
  val toolNameLong:String         = "T-Coffee"
  val toolNameAbbreviation:String = "tcf"


  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "sequences" -> nonEmptyText,
      "mlalign_id_pair" -> boolean,
      "mfast_pair" -> boolean,
      "mslow_pair" -> boolean
    )
  )
}