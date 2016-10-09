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
      Param.ALIGNMENT_FORMAT -> optional(nonEmptyText),
      Param.STANDARD_DB -> optional(text),
      Param.MATRIX -> optional(text),
      Param.NUM_ITER -> optional(number),
      Param.EVALUE -> optional(number),
      Param.GAP_OPEN -> optional(number),
      Param.GAP_EXT -> optional(number),
      Param.DESC -> optional(number),
      Param.CONSISTENCY -> optional(number),
      Param.ITREFINE -> optional(number),
      Param.PRETRAIN -> optional(number),
      Param.MAXROUNDS -> optional(number),
      Param.OFFSET -> optional(number)
    )
  )

  val toolMap : Map[String, ToolModel2] = Map(
    "psiblast" -> PsiBlast,
    "tcoffee" -> Tcoffee,
    "probcons" -> Probcons,
    "muscle" -> Muscle,
    "mafft" -> Mafft
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

  val params = Seq(Param.ALIGNMENT, "alignment_format", "standarddb", "matrix",
    "num_iter", "evalue", "gap_open", "gap", "gap_ext", "desc")
}


object Tcoffee extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort = "tcoffee"
  val toolNameLong = "T-Coffee"
  val toolNameAbbrev = "tcf"
  val category = "alignment"

  val params = Seq(Param.ALIGNMENT)

}

object Probcons extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort = "probcons"
  val toolNameLong = "ProbCons"
  val toolNameAbbrev = "pcns"
  val category = "alignment"

  val params = Seq(Param.ALIGNMENT, Param.CONSISTENCY, Param.ITREFINE, Param.PRETRAIN)

}

object Muscle extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort = "muscle"
  val toolNameLong = "MUSCLE"
  val toolNameAbbrev = "msc"
  val category = "alignment"

  val params = Seq("alignment", "maxrounds")
}

object Mafft extends ToolModel2 {

  val toolNameShort        = "mafft"
  val toolNameLong         = "Mafft"
  val toolNameAbbrev = "mft"
  val category = "alignment"
  val params = Seq(Param.ALIGNMENT, Param.GAP_OPEN, Param.OFFSET)

}