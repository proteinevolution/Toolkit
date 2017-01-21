package models.tools


import com.typesafe.config.{Config, ConfigFactory}
import enumeratum.{PlayEnum, EnumEntry}
import models.tools.ToolModel.Toolitem

import models.{Param, Values}




sealed trait ToolModel extends EnumEntry {

  case class ToolParam(name: String, defvalue: Option[String], paramtype: Int)

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

    "Input" -> Seq(Param.ALIGNMENT.name, Param.ALIGNMENT_FORMAT.name, Param.STANDARD_DB.name, Param.HHSUITEDB.name,
      Param.PROTBLASTPROGRAM.name)
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



  class ToolObject(name: String) {

    lazy val config = ConfigFactory.load()
    lazy val Toolsconf : Config  = config.getConfig("Tools")

    lazy val toolnameShort = Toolsconf.getString(s"$name.name")

  }


  val values : Seq[ToolModel] = findValues // this replaces the toolMap completely


  case object ProtBlast extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "protblast"
    val toolNameLong = "ProtBlast"
    val toolNameAbbrev = "prob"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT.name, "standarddb", "matrix",
      "num_iter", "evalue", Param.EVAL_INC_THRESHOLD.name, "gap_open", "gap_ext", "desc",
      Param.PROTBLASTPROGRAM.name)

    val results = Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")
  }

  case object PatternSearch extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "patternsearch"
    val toolNameLong = "PatternSearch"
    val toolNameAbbrev = "pas"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT.name, "standarddb", "matrix",
      "num_iter", "evalue", Param.EVAL_INC_THRESHOLD.name, "gap_open", "gap_ext", "desc")

    val results = Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")
  }

  case object HHblits extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "hhblits"
    val toolNameLong = "HHblits"
    val toolNameAbbrev = "hhb"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT.name, "hhblitsdb", "maxrounds")

    val results = Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")
  }

  case object HHpred extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "hhpred"
    val toolNameLong = "HHpred"
    val toolNameAbbrev = "hhp"
    val category = "search"
    val optional = ""


    val params : Seq[String] = Seq(Param.ALIGNMENT.name, Param.HHSUITEDB.name, Param.MSAGENERATION.name,
      Param.MSA_GEN_MAX_ITER.name, Param.MIN_COV.name, Param.EVAL_INC_THRESHOLD.name,
      Param.MAX_LINES.name, Param.PMIN.name, Param.ALIWIDTH.name)

    val results = Seq("Hitlist", "Histogram")
  }


  case object PsiBlast extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "psiblast"
    val toolNameLong = "PSI-BLAST"
    val toolNameAbbrev = "pbl"
    val category = "search"
    val optional = ""


    val params = Seq(Param.ALIGNMENT.name, "standarddb", "matrix",
      "num_iter", "evalue", Param.EVAL_INC_THRESHOLD.name, "gap_open", "gap_ext", "desc")

    val results = Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")
  }


  case object Tcoffee extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "tcoffee"
    val toolNameLong = "T-Coffee"
    val toolNameAbbrev = "tcf"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT.name)
    val results = Seq("Alignment", "AlignmentViewer", "Conservation", "Text")
  }


  case object Blammer extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "blammer"
    val toolNameLong = "Blammer"
    val toolNameAbbrev = "blam"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT.name, Param.MIN_QUERY_COV.name, Param.MAX_EVAL.name, Param.MIN_ANCHOR_WITH.name,
      Param.MAX_SEQID.name, Param.MAX_SEQS.name, Param.MIN_COLSCORE.name)
    val results = Seq("Alignment", "AlignmentViewer")
  }


  case object ClustalOmega extends ToolModel {

    val toolNameShort = "clustalo"
    val toolNameLong = "Clustal Omega"
    val toolNameAbbrev = "cluo"
    val category = "alignment"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq("Alignment", "AlignmentViewer")
  }

  /*
  case object GLProbs extends ToolModel {

    val toolNameShort = "glprobs"
    val toolNameLong = "GLProbs"
    val toolNameAbbrev = "glpr"
    val category = "alignment"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq("Alignment", "AlignmentViewer")
  }
  */

  case object MSAProbs extends ToolModel {

    val toolNameShort = "msaprobs"
    val toolNameLong = "MSAProbs"
    val toolNameAbbrev = "msap"
    val category = "alignment"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq("Alignment", "AlignmentViewer")
  }

  /*
  case object Probcons extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "probcons"
    val toolNameLong = "ProbCons"
    val toolNameAbbrev = "pcns"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT.name, Param.CONSISTENCY.name, Param.ITREFINE.name, Param.PRETRAIN.name)

    val results = Seq.empty[String]
  }
  */

  case object Muscle extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "muscle"
    val toolNameLong = "MUSCLE"
    val toolNameAbbrev = "musc"
    val category = "alignment"
    val optional = ""
    val params = Seq("alignment", Param.MAXROUNDS.name)
    val results = Seq("Alignment", "AlignmentViewer")
  }

  case object Mafft extends ToolModel {

    val toolNameShort = "mafft"
    val toolNameLong = "Mafft"
    val toolNameAbbrev = "mft"
    val category = "alignment"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name, Param.GAP_OPEN.name, Param.OFFSET.name)

    val results = Seq("Alignment", "AlignmentViewer")
  }

  case object Kalign extends ToolModel {

    // --- Names for the Tool ---
    val toolNameShort = "kalign"
    val toolNameLong = "Kalign"
    val toolNameAbbrev = "kal"
    val category = "alignment"
    val optional = ""

    val params = Seq(Param.ALIGNMENT.name, Param.GAP_OPEN.name, Param.GAP_EXT.name, Param.GAP_TERM.name, Param.BONUSSCORE.name)

    val results = Seq("Alignment", "AlignmentViewer")
  }

  case object Hmmer extends ToolModel {


    // --- Names for the Tool ---
    val toolNameShort = "hmmer"
    val toolNameLong = "HMMER"
    val toolNameAbbrev = "hmr"
    val category = "search"
    val optional = ""

    val params = Seq(Param.ALIGNMENT.name, Param.STANDARD_DB.name)

    val results = Seq("fileview")
  }


  case object Aln2Plot extends ToolModel {

    val toolNameShort = "aln2plot"
    val toolNameLong = "Aln2Plot"
    val toolNameAbbrev = "a2pl"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq("Hydrophobicity", "SideChainVolume")
  }

  case object PCoils extends ToolModel {

    val toolNameShort = "pcoils"
    val toolNameLong = "PCOILS"
    val toolNameAbbrev = "pco"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name, Param.WEIGHTING.name, Param.MATRIX_PCOILS.name, Param.RUN_PSIPRED.name)
    val results = Seq.empty[String]
  }

  case object FRpred extends ToolModel {

    val toolNameShort = "frpred"
    val toolNameLong = "FRpred"
    val toolNameAbbrev = "frp"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]

  }

  case object HHrep extends ToolModel {

    val toolNameShort = "hhrepid"
    val toolNameLong = "HHrepid"
    val toolNameAbbrev = "hhr"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }

  case object Marcoil extends ToolModel {

    val toolNameShort = "marcoil"
    val toolNameLong = "MARCOIL"
    val toolNameAbbrev = "mar"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name, Param.MATRIX_MARCOIL.name, Param.TRANSITION_PROBABILITY.name)

    val results = Seq("CC-Prob", "ProbList/PSSM", "ProbState", "Domains")
  }

  case object Repper extends ToolModel {

    val toolNameShort = "repper"
    val toolNameLong = "Repper"
    val toolNameAbbrev = "rep"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }

  case object TPRpred extends ToolModel {

    val toolNameShort = "tprpred"
    val toolNameLong = "TPRpred"
    val toolNameAbbrev = "tprp"
    val category = "seqanal"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }

  case object HHomp extends ToolModel {

    val toolNameShort = "hhomp"
    val toolNameLong = "HHomp"
    val toolNameAbbrev = "hho"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }

  case object Quick2D extends ToolModel {

    val toolNameShort = "quick2d"
    val toolNameLong = "Quick2D"
    val toolNameAbbrev = "q2d"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }

  case object Ali2D extends ToolModel {

    val toolNameShort = "ali2d"
    val toolNameLong = "Ali2D"
    val toolNameAbbrev = "a2d"
    val category = "2ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }

  case object Modeller extends ToolModel {

    val toolNameShort = "modeller"
    val toolNameLong = "Modeller"
    val toolNameAbbrev = "mod"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }
  case object Bfit extends ToolModel {

    val toolNameShort = "bfit"
    val toolNameLong = "Bfit"
    val toolNameAbbrev = "bft"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }
  case object HHfrag extends ToolModel {

    val toolNameShort = "hhfrag"
    val toolNameLong = "HHfrag"
    val toolNameAbbrev = "hhf"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }
  case object SamCC extends ToolModel {

    val toolNameShort = "samcc"
    val toolNameLong = "SamCC"
    val toolNameAbbrev = "scc"
    val category = "3ary"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }
  case object ANCESCON extends ToolModel {

    val toolNameShort = "ancescon"
    val toolNameLong = "ANCESCON"
    val toolNameAbbrev = "anc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name, Param.LONG_SEQ_NAME.name)

    val results = Seq("Tree")
  }
  case object PHYLIP extends ToolModel {

    val toolNameShort = "phylip"
    val toolNameLong = "PHYLIP-NEIGHBOR"
    val toolNameAbbrev = "phyn"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name, Param.MATRIX_PHYLIP.name)
    val results = Seq("NeighborJoining", "UPGMA")
  }



  case object CLANS extends ToolModel {

    val toolNameShort = "clans"
    val toolNameLong = "CLANS"
    val toolNameAbbrev = "anc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }
  case object HHcluster extends ToolModel {

    val toolNameShort = "hhcluster"
    val toolNameLong = "HHcluster"
    val toolNameAbbrev = "hhc"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }
  case object BlastClust extends ToolModel {

    val toolNameShort = "blastclust"
    val toolNameLong = "BLASTClust"
    val toolNameAbbrev = "bcl"
    val category = "classification"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name)

    val results = Seq.empty[String]
  }
  case object BackTranslate extends ToolModel {

    val toolNameShort = "backtrans"
    val toolNameLong = "Backtranslator"
    val toolNameAbbrev = "anc"
    val category = "utils"
    val optional = ""
    val params = Seq(Param.ALIGNMENT.name, Param.GENETIC_CODE.name)

    val results = Seq("DNA")
  }
}
