package models.tools

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by lukas on 3/11/16.
  */
object Psiblast extends ToolModel {


  // --- Names for the Tool ---
  val toolNameShort:String        = "psiblast"
  val toolNameLong:String         = "PSI-BLAST"
  val toolNameAbbreviation:String = "pbl"


  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> text,
      "alignment_format" -> text
    )
  )
}
