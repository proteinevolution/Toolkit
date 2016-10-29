package models.tools


import enumeratum.{PlayEnum, EnumEntry}
import models.tools.ToolModel.Toolitem

import models.{Param, Values}
import play.api.data.Form
import play.api.data.Forms._




sealed trait ToolModel extends EnumEntry {



  val toolNameShort : String
  val toolNameLong : String
  val toolNameAbbrev : String
  val category : String
  val optional : String

  // Set of parameter values that are used in the tool
  val params : Seq[String]

  // The results the tool is associated with
  val results : Map[String, String]

  val paramGroups = Map(

    "Input" -> Seq(Param.ALIGNMENT, Param.ALIGNMENT_FORMAT, Param.STANDARD_DB)
  )

  // Params which are not a part of any group
  val remainParamName : String = "Parameters"
  lazy val remainParams : Seq[String] = params.diff(paramGroups.values.flatten.toSeq)

  def toolitem(values : Values) : Toolitem = Toolitem(

    this.toolNameShort,
    this.toolNameLong,
    this.toolNameAbbrev,
    this.optional,
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

object ToolModel extends PlayEnum[ToolModel] {

  case class Toolitem(toolname : String,
                      toolnameLong : String,
                      toolnameAbbrev : String,
                      category : String,
                      optional : String,
                      params : Seq[(String, Seq[(String, Seq[(String, String)])])])

  val values = findValues

  final val jobForm = Form(
    tuple(
      Param.ALIGNMENT -> nonEmptyText, // Input Alignment or input sequences
      Param.ALIGNMENT_FORMAT -> optional(nonEmptyText),
      Param.STANDARD_DB -> optional(text),
      Param.MATRIX -> optional(text),
      Param.NUM_ITER -> optional(number),
      Param.EVALUE -> optional(number),
      Param.ETRESH -> optional(bigDecimal),
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

  val toolMap : Map[String, ToolModel] = Map(
    "psiblast" -> PsiBlast,
    "tcoffee" -> Tcoffee,
    "probcons" -> Probcons,
    "muscle" -> Muscle,
    "mafft" -> Mafft,
    "kalign" -> Kalign,
    "hmmer3" -> Hmmer3,
    "hhcluster" -> HHcluster,
    "ancescon" -> ANCESCON,
    "clans" -> CLANS,
    "bfit" -> Bfit,
    "modeller" -> Modeller,
    "ali2d" -> Ali2D,
    "hhfrag" -> HHfrag,
    "pcoils" -> PCoils,
    "frpred" -> FRpred,
    "hhrep" -> HHrep,
    "marcoil" -> Marcoil,
    "repper" -> Repper,
    "tprpred" -> TPRpred,
    "hhomp" -> HHomp,
    "quick2d" -> Quick2D,
    "samcc" -> SamCC,
    "blastclust" -> BlastClust,
    "hhblits" -> HHblits,
    "hhpred" -> HHpred,
    "patternsearch" -> PatternSearch,
    "blastp" -> BlastP,
    "simshiftdb" -> SimShiftDB
  )


  case object SimShiftDB extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "simshiftdb"
    val toolNameLong = "SimShiftDB"
    val toolNameAbbrev = "ssd"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
      "num_iter", "evalue", "inclusion_ethresh", "gap_open", "gap_ext", "desc")

    val results = Map(
      "Hits" -> "",
      "E-Values" -> "evalues.dat",
      "Fasta" -> "out.align",
      "AlignmentViewer" -> "out.align_clu"
    )
  }

  case object BlastP extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "blastp"
    val toolNameLong = "BLASTP"
    val toolNameAbbrev = "blp"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
      "num_iter", "evalue", "inclusion_ethresh", "gap_open", "gap_ext", "desc")

    val results = Map(
      "Hits" -> "",
      "E-Values" -> "evalues.dat",
      "Fasta" -> "out.align",
      "AlignmentViewer" -> "out.align_clu"
    )
  }

  case object PatternSearch extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "patternsearch"
    val toolNameLong = "PatternSearch"
    val toolNameAbbrev = "pas"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
      "num_iter", "evalue", "inclusion_ethresh", "gap_open", "gap_ext", "desc")

    val results = Map(
      "Hits" -> "",
      "E-Values" -> "evalues.dat",
      "Fasta" -> "out.align",
      "AlignmentViewer" -> "out.align_clu"
    )
  }

  case object HHblits extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "hhblits"
    val toolNameLong = "HHblits"
    val toolNameAbbrev = "hhb"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
      "num_iter", "evalue", "inclusion_ethresh", "gap_open", "gap_ext", "desc")

    val results = Map(
      "Hits" -> "",
      "E-Values" -> "evalues.dat",
      "Fasta" -> "out.align",
      "AlignmentViewer" -> "out.align_clu"
    )
  }

  case object HHpred extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "hhpred"
    val toolNameLong = "HHpred"
    val toolNameAbbrev = "hhp"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
      "num_iter", "evalue", "inclusion_ethresh", "gap_open", "gap_ext", "desc")

    val results = Map(
      "Hits" -> "",
      "E-Values" -> "evalues.dat",
      "Fasta" -> "out.align",
      "AlignmentViewer" -> "out.align_clu"
    )
  }


  case object PsiBlast extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "psiblast"
    val toolNameLong = "PSI-BLAST"
    val toolNameAbbrev = "pbl"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
      "num_iter", "evalue", "inclusion_ethresh", "gap_open", "gap_ext", "desc")

    val results = Map(
      "Hits" -> "",
      "E-Values" -> "evalues.dat",
      "Fasta" -> "out.align",
      "AlignmentViewer" -> "out.align_clu"
    )
  }


  case object Tcoffee extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "tcoffee"
    val toolNameLong = "T-Coffee"
    val toolNameAbbrev = "tcf"
    val category = "alignment"
    val optional = ""


    val params = Seq(Param.ALIGNMENT)
    val results = Map(
      "AlignmentViewer" -> "alignment.clustalw_aln"
    )
  }

  case object Probcons extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "probcons"
    val toolNameLong = "ProbCons"
    val toolNameAbbrev = "pcns"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT, Param.CONSISTENCY, Param.ITREFINE, Param.PRETRAIN)

    val results = Map.empty[String, String]
  }

  case object Muscle extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "muscle"
    val toolNameLong = "MUSCLE"
    val toolNameAbbrev = "msc"
    val category = "alignment"
    val optional = ""

    val params = Seq("alignment", "maxrounds")

    val results = Map.empty[String, String]
  }

  case object Mafft extends ToolModel {

    val toolNameShort = "mafft"
    val toolNameLong = "Mafft"
    val toolNameAbbrev = "mft"
    val category = "alignment"
    val optional = ""
    val params = Seq(Param.ALIGNMENT, Param.GAP_OPEN, Param.OFFSET)

    val results = Map.empty[String, String]

  }

  case object Kalign extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "kalign"
    val toolNameLong = "Kalign"
    val toolNameAbbrev = "kal"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT, Param.OUTORDER, Param.GAP_OPEN, Param.GAP_EXT, Param.GAP_TERM, Param.BONUSSCORE)

    val results = Map.empty[String, String]
  }

  case object Hmmer3 extends ToolModel {


    // --- Names for the Tool ---
    val toolNameShort = "hmmer3"
    val toolNameLong = "Hmmer3"
    val toolNameAbbrev = "hm3"
    val category = "search"
    val optional = ""

    val params = Seq(Param.ALIGNMENT, Param.STANDARD_DB)

    val results = Map(
      "fileview" -> "domtbl"
    )
  }

  case object PCoils extends ToolModel {

    val toolNameShort = "pcoils"
    val toolNameLong = "PCOILS"
    val toolNameAbbrev = "pco"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]

  }

  case object FRpred extends ToolModel {

    val toolNameShort = "frpred"
    val toolNameLong = "FRpred"
    val toolNameAbbrev = "frp"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]

  }

  case object HHrep extends ToolModel {

    val toolNameShort = "hhrep"
    val toolNameLong = "HHrep"
    val toolNameAbbrev = "hhr"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }

  case object Marcoil extends ToolModel {

    val toolNameShort = "marcoil"
    val toolNameLong = "Marcoil"
    val toolNameAbbrev = "mar"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }

  case object Repper extends ToolModel {

    val toolNameShort = "repper"
    val toolNameLong = "Repper"
    val toolNameAbbrev = "rep"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }

  case object TPRpred extends ToolModel {

    val toolNameShort = "tprpred"
    val toolNameLong = "TPRpred"
    val toolNameAbbrev = "tpr"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }

  case object HHomp extends ToolModel {

    val toolNameShort = "hhomp"
    val toolNameLong = "HHomp"
    val toolNameAbbrev = "hho"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }

  case object Quick2D extends ToolModel {

    val toolNameShort = "quick2d"
    val toolNameLong = "Quick2D"
    val toolNameAbbrev = "q2d"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }

  case object Ali2D extends ToolModel {

    val toolNameShort = "ali2d"
    val toolNameLong = "Ali2D"
    val toolNameAbbrev = "a2d"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }

  case object Modeller extends ToolModel {

    val toolNameShort = "modeller"
    val toolNameLong = "Modeller"
    val toolNameAbbrev = "mod"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }
  case object Bfit extends ToolModel {

    val toolNameShort = "bfit"
    val toolNameLong = "Bfit"
    val toolNameAbbrev = "bft"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }
  case object HHfrag extends ToolModel {

    val toolNameShort = "hhfrag"
    val toolNameLong = "HHfrag"
    val toolNameAbbrev = "hhf"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }
  case object SamCC extends ToolModel {

    val toolNameShort = "samcc"
    val toolNameLong = "SamCC"
    val toolNameAbbrev = "scc"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }
  case object ANCESCON extends ToolModel {

    val toolNameShort = "ancescon"
    val toolNameLong = "ANCESCON"
    val toolNameAbbrev = "anc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }
  case object CLANS extends ToolModel {

    val toolNameShort = "clans"
    val toolNameLong = "CLANS"
    val toolNameAbbrev = "anc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }
  case object HHcluster extends ToolModel {

    val toolNameShort = "hhcluster"
    val toolNameLong = "HHcluster"
    val toolNameAbbrev = "hhc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }
  case object BlastClust extends ToolModel {

    val toolNameShort = "blastclust"
    val toolNameLong = "BLASTClust"
    val toolNameAbbrev = "bcl"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Map.empty[String, String]
  }
}
