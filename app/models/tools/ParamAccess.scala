package models.tools


import javax.inject.{Inject, Singleton}

import modules.tel.TEL
import play.api.libs.json._



sealed trait ParamType
case object Sequence extends ParamType
case object Number   extends ParamType
case object Select   extends ParamType
case object Bool     extends ParamType
case object Radio    extends ParamType

object ParamType {
  implicit object ParamTypeWrites extends Writes[ParamType] {
    def writes(paramType: ParamType): JsNumber = paramType match {
      case Sequence => JsNumber(1)
      case Number => JsNumber(2)
      case Select => JsNumber(3)
      case Bool => JsNumber(4)
      case Radio => JsNumber(5)
    }
  }
}




object Validators {

  abstract class Validator {

    def apply(x : String): Option[String]
  }

  object AcceptAll extends Validator {

    override def apply(x: String) = None
  }
  object IsInteger extends Validator {

    override def apply(x: String): Option[String] = {
      try {
        val y = x.toInt
        None
      } catch  {
        case _: NumberFormatException => Some(s"$x is not an integer")
      }
    }
  }
}




/** @author snam
  * case class handles ordering of param tabs
  * @param name: value for cli usage of parameters in the runscripts
  * @param internalOrdering: allows to order the items within the params tab
  */
case class Param(name: String,
                 paramType: ParamType,
                 internalOrdering: Int,
                 label: String,  // Label of the parameter
                 validators: Seq[Validators.Validator]) {

  // Constructor for Parameter which accepts all arguments
  def this(name: String, paramType: ParamType, internalOrdering: Int, label: String) =
    this(name, paramType, internalOrdering, label, Seq(Validators.AcceptAll))
}

/**
  * Provides the specification of the Parameters as they appear in the individual tools
  *
  * @param tel access to the allowedvalues of certain Parameters
  */
@Singleton
class ParamAccess @Inject() (tel: TEL) {

  // Shared parameters by all tools
  final val ALIGNMENT = new Param("alignment", Sequence,1, "Enter multiple sequence alignment")
  final val ALIGNMENT_FORMAT =  new Param("alignment_format",Select,1, "Select alignment format")
  final val STANDARD_DB = new Param("standarddb",Select,1, "Select standard database")
  final val HHSUITEDB = new Param("hhsuitedb",Select,1, "Select HH-Suite database")
  final val MATRIX = new Param("matrix",Select,1, "Scoring matrix")
  final val NUM_ITER = new Param("num_iter",Number,1, "No. of iterations")
  final val EVALUE = new Param("evalue",Number,1, "Evalue")
  final val GAP_OPEN = new Param("gap_open",Number,1, "Gap open penalty")
  final val GAP_EXT = new Param("gap_ext",Number,1, "Gap extension penalty")
  final val GAP_TERM = new Param("gap_term",Number,1, "Gap termination penalty")
  final val DESC = new Param("desc",Number,1, "No. of descriptions")
  final val CONSISTENCY =  new Param("consistency",Number,1, "Passes of consistency transformation")
  final val ITREFINE = new Param("itrefine",Number,1, "Passes of iterative refinements")
  final val PRETRAIN =  new Param("pretrain",Number,1, "Rounds of pretraining")
  final val MAXROUNDS = new Param("maxrounds",Number,1, "Maximum number of iterations")
  final val OFFSET = new Param("offset",Number,1, "Offset")
  final val BONUSSCORE = new Param("bonusscore",Number,1, "Bonus Score")
  final val OUTORDER = new Param("outorder",Number,1, "Outorder")
  final val ETRESH = new Param("inclusion_ethresh",Number,1, "E-value inclusion threshold")
  final val HHBLITSDB  =  new Param("hhblitsdb",Select,1, "Select HHblts database")
  final val ALIGNMODE = new Param("alignmode",Select,1, "Alignment Mode")
  final val MSAGENERATION = new Param("msageneration",Select,1, "Method of MSA generation")
  final val MSA_GEN_MAX_ITER = Param("msa_gen_max_iter",Select,1, "Max. number of MSA generation iterations",
    Seq(Validators.IsInteger))
  final val GENETIC_CODE = new Param("genetic_code",Select,1, "Choose a genetic code")
  final val LONG_SEQ_NAME = new Param("long_seq_name",Bool,1, "Use long sequence name")
  final val EVAL_INC_THRESHOLD = new Param("inclusion_ethresh",Select,1, "E-value inclusion threshold")
  final val MIN_COV = new Param("min_cov",Number,1, "Min. coverage of hits")
  final val MAX_LINES = new Param("max_lines",Number,1, "Max. number of hits in hitlist")
  final val PMIN = new Param("pmin",Number,1, "Min. probability in hitlist")
  final val MAX_SEQS = new Param("max_seqs",Number,1, "Max. number of sequences")
  final val ALIWIDTH = new Param("aliwidth",Number,1, "With of alignments (columns)")
  final val MAX_EVAL = new Param("max_eval",Number, 1, "Maximal E-Value")
  final val MAX_SEQID = new Param("max_seqid", Number, 1, "Maximal Sequence Identity")
  final val MIN_COLSCORE = new Param("min_colscore", Number, 1, "Minimal Column Score")
  final val MIN_QUERY_COV = new Param("min_query_cov", Number, 1, "Minimal coverage of query")
  final val MIN_ANCHOR_WITH = new Param("min_anchor_width", Number, 1, "Minimal Anchor width")
  final val WEIGHTING = new Param("weighting", Bool, 1, "Weighting")
  final val RUN_PSIPRED = new Param("run_psipred", Bool,1, "Run PSIPRED")
  final val MATRIX_PHYLIP = new Param("matrix_phylip",Select,1, "Model of amino acid replacement")
  final val MATRIX_PCOILS = new Param("matrix_pcoils", Select, 1, "Matrix")
  final val PROTBLASTPROGRAM = new Param("protblastprogram", Select, 1, "Program for performing Protein BLAST search")
  final val FILTER_LOW_COMPLEXITY =  new Param("filter_low_complexity", Bool, 1, "Filter for low complexity regions")
  final val MATRIX_MARCOIL = new Param("matrix_marcoil", Select, 1, "Matrix")
  final val TRANSITION_PROBABILITY = new Param("transition_probability", Select, 1, "Transition Probability'")
  final val MIN_SEQID_QUERY = new Param("min_seqid_query", Number, 1, "Minimum sequence ID with Query (%)")
  final val NUM_SEQS_EXTRACT =  new Param("num_seqs_extract", Number, 1, "No. of sequences to extract")

  // Maps parameter values onto their full names descriptions, as they should appear in the view
  final val fullNames = Map(

    "fas" -> "FASTA",
    "clu" -> "CLUSTALW",
    "sto" -> "Stockholm",
    "a2m" -> "A2M",
    "a3m" -> "A3M",
    "emb" -> "EMBL",
    "meg" -> "MEGA",
    "msf" -> "GCG/MSF",
    "pir" -> "PIR/NBRF",
    "tre" -> "TREECON",
    "BLOSUM62" -> "BLOSUM62",
    "BLOSUM45" -> "BLOSUM45",
    "BLOSUM80" -> "BLOSUM80",
    "PAM30" -> "PAM30",
    "PAM70" -> "PAM70"
  )

  // TODO This will go soon
  final val alignmentFormats = Set("fas", "clu", "sto", "a2m", "a3m", "emb", "meg", "msf", "pir", "tre")
  final val matrixParams = Set("BLOSUM62", "BLOSUM45", "BLOSUM80", "PAM30", "PAM70")
  final val outOrderParams = Set("Input", "Tree", "Gaps")
  // ------

  // encompasses for certain parameters the allowed values with a clear text name
  // TODO Needs to be reloaded if TEL refreshed the parameter lists
  final val allowed: Map[String, Seq[(String, String)]] = Map(
    ALIGNMENT.name -> alignmentFormats.map { format =>

      format -> fullNames(format)
    }.toSeq,
    STANDARD_DB.name -> tel.generateValues(STANDARD_DB.name).toSeq,
    HHBLITSDB.name -> tel.generateValues(HHBLITSDB.name).toSeq,
    HHSUITEDB.name -> tel.generateValues(HHSUITEDB.name).toSeq,
    MSAGENERATION.name -> tel.generateValues(MSAGENERATION.name).toSeq,
    MSA_GEN_MAX_ITER.name -> tel.generateValues(MSA_GEN_MAX_ITER.name).toSeq.sortBy(_._1),
    GENETIC_CODE.name -> tel.generateValues(GENETIC_CODE.name).toSeq,
    MATRIX.name -> matrixParams.map { matrix =>

      matrix -> fullNames(matrix)
    }.toSeq,
    EVAL_INC_THRESHOLD.name -> tel.generateValues(EVAL_INC_THRESHOLD.name).toSeq.sortBy(_._1.toFloat),
    MIN_COV.name -> tel.generateValues(MIN_COV.name).toSeq.sorted,
    MATRIX_PHYLIP.name -> tel.generateValues(MATRIX_PHYLIP.name).toSeq.sorted,
    MATRIX_PCOILS.name -> tel.generateValues(MATRIX_PCOILS.name).toSeq.sorted,
    PROTBLASTPROGRAM.name -> tel.generateValues(PROTBLASTPROGRAM.name).toSeq.sorted,
    MATRIX_MARCOIL.name -> tel.generateValues(MATRIX_MARCOIL.name).toSeq.sorted,
    TRANSITION_PROBABILITY.name -> tel.generateValues(TRANSITION_PROBABILITY.name).toSeq.sorted,
    ALIGNMODE.name -> tel.generateValues(ALIGNMODE.name).toSeq.sorted
  )
}
