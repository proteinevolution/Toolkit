package models.tools

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by lzimmermann on 8/14/16.
  */
object HHblits extends ToolModel{

  // --- Names for the Tool ---
  val toolNameShort:String        = "hhblits"
  val toolNameLong:String         = "hhblits"
  val toolNameAbbreviation:String = "HHBL"


  // --- HHPRED
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text
    )
  )
}
