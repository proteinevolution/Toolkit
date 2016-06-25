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
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "gap_open" -> number,
      "gap_ext" -> number,
      "desc" -> number,
      "standarddb" -> text
    )
  )
  val parameterValues = Map(
    "matrix" -> Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70"),
    "alignment_format" -> Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  )


}
