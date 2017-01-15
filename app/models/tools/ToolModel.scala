package models.tools


import enumeratum.{PlayEnum, EnumEntry}
import models.tools.ToolModel.Toolitem

import models.{Param, Values}




sealed trait ToolModel extends EnumEntry {



  val toolNameShort : String
  val toolNameLong : String
  val toolNameAbbrev : String
  val category : String
  val optional : String

  // Set of parameter values that are used in the tool
  val params : Seq[String]

  // The results the tool is associated with
  val results : Seq[String]

  val paramGroups = Map(

    "Input" -> Seq(Param.ALIGNMENT, Param.ALIGNMENT_FORMAT, Param.STANDARD_DB, Param.HHSUITEDB)
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

  val values : Seq[ToolModel] = findValues

  val toolMap : Map[String, ToolModel] = Map(
    "psiblast" -> PsiBlast,
    "tcoffee" -> Tcoffee,
    "probcons" -> Probcons,
    "muscle" -> Muscle,
    "mafft" -> Mafft,
    "kalign" -> Kalign,
    "hmmer" -> Hmmer,
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
    "backtrans" -> BackTranslate
  )


  case object BlastP extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "blastp"
    val toolNameLong = "BLASTP"
    val toolNameAbbrev = "blp"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
      "num_iter", "evalue", "inclusion_ethresh", "gap_open", "gap_ext", "desc")

    val results = Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")

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

    val results = Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")
  }

  case object HHblits extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "hhblits"
    val toolNameLong = "HHblits"
    val toolNameAbbrev = "hhb"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "hhblitsdb", "maxrounds")

    val results = Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")
  }

  case object HHpred extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "hhpred"
    val toolNameLong = "HHpred"
    val toolNameAbbrev = "hhp"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, Param.HHSUITEDB, Param.MSAGENERATION, Param.MSA_GEN_MAX_ITER, Param.EVAL_INC_THRESHOLD)

    val results = Seq("Hitlist", "Histogram")
  }


  case object PsiBlast extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "psiblast"
    val toolNameLong = "PSI-BLAST"
    val toolNameAbbrev = "pbl"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT, "standarddb", "matrix",
      "num_iter", "evalue", Param.EVAL_INC_THRESHOLD, "gap_open", "gap_ext", "desc")

    val results = Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")
  }


  case object Tcoffee extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "tcoffee"
    val toolNameLong = "T-Coffee"
    val toolNameAbbrev = "tcf"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT)
    val results = Seq("Alignment", "AlignmentViewer", "Conservation", "Text")
  }

  case object Probcons extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "probcons"
    val toolNameLong = "ProbCons"
    val toolNameAbbrev = "pcns"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT, Param.CONSISTENCY, Param.ITREFINE, Param.PRETRAIN)

    val results = Seq.empty[String]
  }

  case object Muscle extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "muscle"
    val toolNameLong = "MUSCLE"
    val toolNameAbbrev = "msc"
    val category = "alignment"
    val optional = ""

    val params = Seq("alignment", "maxrounds")

    val results = Seq.empty[String]
  }

  case object Mafft extends ToolModel {

    val toolNameShort = "mafft"
    val toolNameLong = "Mafft"
    val toolNameAbbrev = "mft"
    val category = "alignment"
    val optional = ""
    val params = Seq(Param.ALIGNMENT, Param.GAP_OPEN, Param.OFFSET)

    val results = Seq.empty[String]

  }

  case object Kalign extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "kalign"
    val toolNameLong = "Kalign"
    val toolNameAbbrev = "kal"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT, Param.OUTORDER, Param.GAP_OPEN, Param.GAP_EXT, Param.GAP_TERM, Param.BONUSSCORE)

    val results = Seq.empty[String]
  }

  case object Hmmer extends ToolModel {


    // --- Names for the Tool ---
    val toolNameShort = "hmmer"
    val toolNameLong = "HMMER"
    val toolNameAbbrev = "hmr"
    val category = "search"
    val optional = ""

    val params = Seq(Param.ALIGNMENT, Param.STANDARD_DB)

    val results = Seq("fileview")
  }

  case object PCoils extends ToolModel {

    val toolNameShort = "pcoils"
    val toolNameLong = "PCOILS"
    val toolNameAbbrev = "pco"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]

  }

  case object FRpred extends ToolModel {

    val toolNameShort = "frpred"
    val toolNameLong = "FRpred"
    val toolNameAbbrev = "frp"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]

  }

  case object HHrep extends ToolModel {

    val toolNameShort = "hhrep"
    val toolNameLong = "HHrep"
    val toolNameAbbrev = "hhr"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }

  case object Marcoil extends ToolModel {

    val toolNameShort = "marcoil"
    val toolNameLong = "Marcoil"
    val toolNameAbbrev = "mar"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }

  case object Repper extends ToolModel {

    val toolNameShort = "repper"
    val toolNameLong = "Repper"
    val toolNameAbbrev = "rep"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }

  case object TPRpred extends ToolModel {

    val toolNameShort = "tprpred"
    val toolNameLong = "TPRpred"
    val toolNameAbbrev = "tpr"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }

  case object HHomp extends ToolModel {

    val toolNameShort = "hhomp"
    val toolNameLong = "HHomp"
    val toolNameAbbrev = "hho"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }

  case object Quick2D extends ToolModel {

    val toolNameShort = "quick2d"
    val toolNameLong = "Quick2D"
    val toolNameAbbrev = "q2d"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }

  case object Ali2D extends ToolModel {

    val toolNameShort = "ali2d"
    val toolNameLong = "Ali2D"
    val toolNameAbbrev = "a2d"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }

  case object Modeller extends ToolModel {

    val toolNameShort = "modeller"
    val toolNameLong = "Modeller"
    val toolNameAbbrev = "mod"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }
  case object Bfit extends ToolModel {

    val toolNameShort = "bfit"
    val toolNameLong = "Bfit"
    val toolNameAbbrev = "bft"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }
  case object HHfrag extends ToolModel {

    val toolNameShort = "hhfrag"
    val toolNameLong = "HHfrag"
    val toolNameAbbrev = "hhf"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }
  case object SamCC extends ToolModel {

    val toolNameShort = "samcc"
    val toolNameLong = "SamCC"
    val toolNameAbbrev = "scc"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }
  case object ANCESCON extends ToolModel {

    val toolNameShort = "ancescon"
    val toolNameLong = "ANCESCON"
    val toolNameAbbrev = "anc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT, Param.LONG_SEQ_NAME)

    val results = Seq("Tree")
  }
  case object CLANS extends ToolModel {

    val toolNameShort = "clans"
    val toolNameLong = "CLANS"
    val toolNameAbbrev = "anc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }
  case object HHcluster extends ToolModel {

    val toolNameShort = "hhcluster"
    val toolNameLong = "HHcluster"
    val toolNameAbbrev = "hhc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }
  case object BlastClust extends ToolModel {

    val toolNameShort = "blastclust"
    val toolNameLong = "BLASTClust"
    val toolNameAbbrev = "bcl"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT)

    val results = Seq.empty[String]
  }
  case object BackTranslate extends ToolModel {

    val toolNameShort = "backtrans"
    val toolNameLong = "Backtranslator"
    val toolNameAbbrev = "anc"
    val category = "utils"
    val optional = ""
    val params = Seq(Param.ALIGNMENT, Param.GENETIC_CODE)

    val results = Seq("DNA")
  }
}
