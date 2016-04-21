package models.tools

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by lzimmermann on 21.04.16.
  */
object Csblast extends ToolModel {

  // --- Names for the Tool ---
  val toolNameShort:String        = "csblast"
  val toolNameLong:String         = "CS-BLAST"
  val toolNameAbbreviation:String = "cbl"

  // --- Tcoffee specific values ---
  // Returns the Input Form Definition of this tool
  val inputForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "gap_open" -> number,
      "gap_ext" -> number,
      "desc" -> number
    )
  )

  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )
}