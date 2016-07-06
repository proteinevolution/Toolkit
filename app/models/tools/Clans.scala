package models.tools

/**
  * Created by zin on 06.07.16.
  */
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
  val toolNameAbbreviation:String = "cla"




  // --- Clans specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "standarddb" -> text
    )
  )
}
