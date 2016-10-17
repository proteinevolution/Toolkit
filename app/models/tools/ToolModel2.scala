package models.tools


import models.{Param, Values}
import models.tools.ToolModel2.Toolitem
import play.api.data.Form
import play.api.data.Forms._

/**
  * Created by lzimmermann on 10/8/16.
  */


object ToolModel2 {


  case class Toolitem(toolname : String,
                      toolnameLong : String,
                      toolnameAbbrev : String,
                      category : String,
                      params : Seq[(String, Seq[(String, Seq[(String, String)])])])
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
      Param.GAP_TERM -> optional(number),
      Param.DESC -> optional(number),
      Param.CONSISTENCY -> optional(number),
      Param.ITREFINE -> optional(number),
      Param.PRETRAIN -> optional(number),
      Param.MAXROUNDS -> optional(number),
      Param.OFFSET -> optional(number),
      Param.BONUSSCORE -> optional(number),
      Param.OUTORDER -> optional(text)
    )
  )

  val toolMap : Map[String, ToolModel2] = Map(
    "psiblast" -> PsiBlast,
    "tcoffee" -> Tcoffee,
    "probcons" -> Probcons,
    "muscle" -> Muscle,
    "mafft" -> Mafft,
    "kalign" -> Kalign,
    "hmmer3" -> Hmmer3
  )



}

abstract class ToolModel2  {

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

  // The results the tool is associated with
  val results : Map[String, String]

  def toolitem(values : Values) : Toolitem = Toolitem(

    this.toolNameShort,
    this.toolNameLong,
    this.toolNameAbbrev,
    this.category, this.paramGroups.keysIterator.map { group =>

      group ->  this.paramGroups(group).filter(this.params.contains(_)).map { param =>

        param -> values.allowed.getOrElse(param, Seq.empty)
      }
    }.toSeq :+
      this.remainParamName -> this.remainParams.map { param =>

        param -> values.allowed.getOrElse(param, Seq.empty)

      }
  )
}


object PsiBlast extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort       = "psiblast"
  val toolNameLong        = "PSI-BLAST"
  val toolNameAbbrev = "pbl"
  val category = "search"


  val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
    "num_iter", "evalue", "gap_open", "gap_ext", "desc")

  val results = Map(
    "blast" -> "",
    "evalue" -> "evalues.dat",
    "fasta" -> "out.align",
    "biojs" -> "out.align"
  )
}


object Tcoffee extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort = "tcoffee"
  val toolNameLong = "T-Coffee"
  val toolNameAbbrev = "tcf"
  val category = "alignment"

  val params = Seq(Param.ALIGNMENT)

  val results = Map(

    "simple" -> "alignment.clustalw_aln",
    "biojs" -> "alignment.clustalw_aln"
  )
}

object Probcons extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort = "probcons"
  val toolNameLong = "ProbCons"
  val toolNameAbbrev = "pcns"
  val category = "alignment"

  val params = Seq(Param.ALIGNMENT, Param.CONSISTENCY, Param.ITREFINE, Param.PRETRAIN)

  val results = Map.empty[String, String]
}

object Muscle extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort = "muscle"
  val toolNameLong = "MUSCLE"
  val toolNameAbbrev = "msc"
  val category = "alignment"

  val params = Seq("alignment", "maxrounds")

  val results = Map.empty[String, String]
}

object Mafft extends ToolModel2 {

  val toolNameShort        = "mafft"
  val toolNameLong         = "Mafft"
  val toolNameAbbrev = "mft"
  val category = "alignment"
  val params = Seq(Param.ALIGNMENT, Param.GAP_OPEN, Param.OFFSET)

  val results = Map.empty[String, String]

}

object Kalign extends ToolModel2 {

  // --- Names for the Tool ---
  val toolNameShort = "kalign"
  val toolNameLong = "Kalign"
  val toolNameAbbrev = "kal"
  val category = "alignment"

  val params = Seq(Param.ALIGNMENT, Param.OUTORDER, Param.GAP_OPEN, Param.GAP_EXT, Param.GAP_TERM, Param.BONUSSCORE)

  val results = Map.empty[String, String]
}

object Hmmer3 extends ToolModel2 {


  // --- Names for the Tool ---
  val toolNameShort        = "hmmer3"
  val toolNameLong         = "Hmmer3"
  val toolNameAbbrev       = "hm3"
  val category = "search"

  val params = Seq(Param.ALIGNMENT, Param.ALIGNMENT_FORMAT, Param.STANDARD_DB)

  val results = Map(
    "fileview" -> "domtbl"
  )
}
