package models.tools

import models.Param
import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by lzimmermann on 10/8/16.
  */
object ToolModel2 {

  /*
    Specifies the form mapping of the parameters
  */
  final val jobForm = Form(
    tuple(
      Param.ALIGNMENT -> nonEmptyText, // Input Alignment or input sequences
      Param.ALIGNMENT_FORMAT -> nonEmptyText,
      Param.STANDARD_DB -> text,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "gap_open" -> number,
      "gap_ext" -> number,
      "desc" -> number
    )
  )

  val toolMap : Map[String, ToolModel2] = Map(
    "psiblast" -> PsiBlast
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

   "Alignment" -> Seq(Param.ALIGNMENT, Param.ALIGNMENT_FORMAT, Param.STANDARD_DB)
  )


  // Params which are not a part of any group
  val remainParamName : String = "Parameter"
  lazy val remainParams : Seq[String] = params.diff(paramGroups.values.flatten.toSeq)
}


object PsiBlast extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort       = "psiblast"
  val toolNameLong        = "PSI-BLAST"
  val toolNameAbbrev = "pbl"
  val category = "search"

  val params = Seq("alignment", "alignment_format", "standarddb", "matrix",
    "num_iter", "evalue", "gap_open", "gap", "gap_ext", "desc")
}



