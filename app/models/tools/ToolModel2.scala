package models.tools

import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by lzimmermann on 10/8/16.
  */
object ToolModel2 {

  /*
    Template Form for all Tools
  */
  val jobForm = Form(
    tuple(
      "alignment_sequences" -> nonEmptyText, // Input Alignment or input sequences
      "alignment_format" -> text,
      "standarddb" -> text,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "gap_open" -> number,
      "gap_ext" -> number,
      "desc" -> number
    )
  )
}

abstract class ToolModel2 {


  val toolNameShort : String
  val toolNameLong : String
  val toolNameAbbrev : String
  val category : String

  // Set of parameter values that are used in the tool
  val params : Seq[String]
  val paramGroups = Map(

   "Alignment" -> Seq("alignment_sequences", "alignment_format")
  )

  val paramRemainderName : String = "Parameter"
}


object PsiBlast extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort       = "psiblast"
  val toolNameLong        = "PSI-BLAST"
  val toolNameAbbrev = "pbl"
  val category = "search"

  val params = Seq("alignment_sequences", "alignment_format", "standarddb", "matrix",
    "num_iter", "evalue", "gap_open", "gap", "gap_ext", "desc")
}



