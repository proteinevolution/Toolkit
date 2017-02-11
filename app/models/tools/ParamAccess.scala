package models.tools



import modules.tel.TEL
import javax.inject.{Inject, Singleton}
import play.api.libs.functional.syntax._
import play.api.libs.json._



// Modes in which Sequences might be entered
abstract class SequenceMode(val label: String)
case class Alignment(formats: Seq[(String,String)]) extends SequenceMode("Multiple Sequence Alignment")
case object SingleSequence extends SequenceMode("Single Sequence")
case object MultiSequence extends SequenceMode("Muliple Sequences")
case object BLASTHTML extends SequenceMode("BLAST HTML page")     // BLAMMER
case object PIR extends SequenceMode("PIR Format")


object SequenceMode {

  implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[(A, B)] = new Writes[(A, B)] {
    def writes(tuple: (A, B)) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
  }

  implicit object ParamTypeWrites extends Writes[SequenceMode] {

    final val FIELD_MODE = "mode"
    final val FIELD_LABEL = "label"
    final val FIELD_FORMATS = "formats"

    def writes(sequenceMode: SequenceMode): JsObject = sequenceMode match {
      case a@Alignment(formats) =>       Json.obj(FIELD_MODE -> 1, FIELD_LABEL -> a.label, FIELD_FORMATS -> formats)
      case SingleSequence =>  Json.obj(FIELD_MODE -> 2, FIELD_LABEL -> SingleSequence.label)
      case MultiSequence =>   Json.obj(FIELD_MODE -> 3, FIELD_LABEL -> MultiSequence.label)
      case BLASTHTML =>       Json.obj(FIELD_MODE -> 4 , FIELD_LABEL -> BLASTHTML.label)
      case PIR =>             Json.obj(FIELD_MODE -> 5 , FIELD_LABEL -> PIR.label)
    }
  }
}



sealed trait ParamType
case class Sequence(modes: Seq[SequenceMode]) extends ParamType
case class Number(min: Option[Int], max: Option[Int]) extends ParamType
case class Select(options: Seq[(String, String)])   extends ParamType
case object Bool     extends ParamType
case object Radio    extends ParamType

object ParamType {

  implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[(A, B)] = new Writes[(A, B)] {
    def writes(tuple: (A, B)) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
  }

  final val UnconstrainedNumber = Number(None, None)
  final val Percentage = Number(Some(0), Some(100))

  // JSON conversion
  final val FIELD_TYPE = "type"
  implicit object ParamTypeWrites extends Writes[ParamType] {

    def writes(paramType: ParamType): JsObject = paramType match {

      case Sequence(modes) => Json.obj(FIELD_TYPE -> 1, "modes" -> modes)
      case Number(minOpt, maxOpt) => Json.obj(FIELD_TYPE -> 2, "min" -> minOpt, "max" -> maxOpt)
      case Select(options) => Json.obj(FIELD_TYPE -> 3, "options" -> options)
      case Bool => Json.obj(FIELD_TYPE -> 4)
      case Radio => Json.obj(FIELD_TYPE -> 5)
    }
  }
}


// A simple parameter with name and a type
case class Param(name: String,
                 paramType: ParamType,
                 internalOrdering: Int,
                 label: String)

object Param {
  implicit val paramWrites: Writes[Param] = (
    (JsPath \ "name").write[String] and
      (JsPath \ "paramType").write[ParamType] and
      (JsPath \ "internalOrdering").write[Int] and
      (JsPath \ "label").write[String]
    ) (unlift(Param.unapply))
}


/**
  * Provides the specification of the Parameters as they appear in the individual tools
  **/
@Singleton
class ParamAccess @Inject() (tel: TEL) {

  def select(name: String, label: String) = Param(name, Select(tel.generateValues(name).toSeq.sortBy(_._2)),1,label)

  final val alignmentFormats = Seq(
    "fas" -> "FASTA",
    "clu" -> "CLUSTALW",
    "sto" -> "Stockholm",
    "a2m" -> "A2M",
    "a3m" -> "A3M",
    "emb" -> "EMBL",
    "meg" -> "MEGA",
    "msf" -> "GCG/MSF",
    "pir" -> "PIR/NBRF",
    "tre" -> "TREECON"
  )

  final val ALIGNMENT = Param("alignment", Sequence(Seq(Alignment(alignmentFormats))),1, "Enter multiple sequence alignment")
  final val STANDARD_DB = select("standarddb", "Select Standard Database")
  final val HHSUITEDB = select("hhsuitedb", "Select HH-Suite Database")
  final val MATRIX = select("matrix", "Scoring Matrix")
  final val NUM_ITER = Param("num_iter",ParamType.UnconstrainedNumber,1, "No. of iterations")
  final val EVALUE = Param("evalue",ParamType.UnconstrainedNumber,1, "Evalue")
  final val GAP_OPEN = Param("gap_open",ParamType.UnconstrainedNumber,1, "Gap open penalty")
  final val GAP_EXT = Param("gap_ext",ParamType.UnconstrainedNumber,1, "Gap extension penalty")
  final val GAP_TERM = Param("gap_term",ParamType.UnconstrainedNumber,1, "Gap termination penalty")
  final val DESC = Param("desc",ParamType.UnconstrainedNumber,1, "No. of descriptions")
  final val CONSISTENCY =  Param("consistency",ParamType.UnconstrainedNumber,1, "Passes of consistency transformation")
  final val ITREFINE = Param("itrefine",ParamType.UnconstrainedNumber,1, "Passes of iterative refinements")
  final val PRETRAIN =  Param("pretrain",ParamType.UnconstrainedNumber,1, "Rounds of pretraining")
  final val MAXROUNDS = Param("maxrounds",ParamType.UnconstrainedNumber,1, "Maximum number of iterations")
  final val OFFSET = Param("offset",ParamType.UnconstrainedNumber,1, "Offset")
  final val BONUSSCORE = Param("bonusscore",ParamType.UnconstrainedNumber,1, "Bonus Score")
  final val OUTORDER = Param("outorder",ParamType.UnconstrainedNumber,1, "Outorder")
  final val ETRESH = Param("inclusion_ethresh",ParamType.UnconstrainedNumber,1, "E-value inclusion threshold")
  final val HHBLITSDB  =  Param("hhblitsdb",Select(tel.generateValues("hhblitsdb").toSeq),1, "Select HHblts database")
  final val ALIGNMODE = select("alignmode", "Alignment Mode")
  final val MSAGENERATION = select("msageneration", "Method of MSA generation")
  final val MSA_GEN_MAX_ITER = select("msa_gen_max_iter", "Maximal no. of MSA generation steps")
  final val GENETIC_CODE = select("genetic_code", "Genetic Code")
  final val LONG_SEQ_NAME =  Param("long_seq_name",Bool,1, "Use long sequence name")
  final val EVAL_INC_THRESHOLD = select("inclusion_ethresh", "E-Value inclusion threshold")
  final val MIN_COV = Param("min_cov",ParamType.Percentage, 1, "Min. coverage of hits (%)")
  final val MAX_LINES = Param("max_lines",ParamType.UnconstrainedNumber,1, "Max. number of hits in hitlist")
  final val PMIN = Param("pmin",ParamType.Percentage,1, "Min. probability in hitlist (%)")
  final val MAX_SEQS = Param("max_seqs",ParamType.UnconstrainedNumber,1, "Max. number of sequences")
  final val ALIWIDTH = Param("aliwidth", Number(Some(0),Some(100)),1, "With of alignments (columns)")
  final val MAX_EVAL = Param("max_eval",ParamType.UnconstrainedNumber, 1, "Maximal E-Value")
  final val MAX_SEQID =  Param("max_seqid", ParamType.UnconstrainedNumber, 1, "Maximal Sequence Identity")
  final val MIN_COLSCORE = Param("min_colscore", ParamType.UnconstrainedNumber, 1, "Minimal Column Score")
  final val MIN_QUERY_COV = Param("min_query_cov", ParamType.UnconstrainedNumber, 1, "Minimal coverage of query")
  final val MIN_ANCHOR_WITH = Param("min_anchor_width", ParamType.UnconstrainedNumber, 1, "Minimal Anchor width")
  final val WEIGHTING = Param("weighting", Bool, 1, "Weighting")
  final val RUN_PSIPRED = Param("run_psipred", Bool,1, "Run PSIPRED")
  final val MATRIX_PHYLIP = select("matrix_phylip", "Model of AminoAcid replacement")
  final val MATRIX_PCOILS = select("matrix_pcoils", "Matrix")
  final val PROTBLASTPROGRAM = select("protblastprogram", "Program for Protein BLAST")
  final val FILTER_LOW_COMPLEXITY = Param("filter_low_complexity", Bool, 1, "Filter for low complexity regions")
  final val MATRIX_MARCOIL =  select("matrix_marcoil", "Matrix")
  final val TRANSITION_PROBABILITY = select("transition_probability", "Transition Probability")
  final val MIN_SEQID_QUERY = Param("min_seqid_query", ParamType.UnconstrainedNumber, 1, "Minimum sequence ID with Query (%)")
  final val NUM_SEQS_EXTRACT =  Param("num_seqs_extract", ParamType.UnconstrainedNumber, 1, "No. of sequences to extract")
  final val SS_SCORING = select("ss_scoring", "SS Scoring")
}
