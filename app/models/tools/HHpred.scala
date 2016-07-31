package models.tools

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by lzimmermann on 7/2/16.
  */
object HHpred extends ToolModel {


  // --- Names for the Tool ---
  val toolNameShort:String        = "hhpred"
  val toolNameLong:String         = "HHpred"
  val toolNameAbbreviation:String = "HHPR"


  // --- HHPRED
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "hhmdb" -> text
    )
  )
}
