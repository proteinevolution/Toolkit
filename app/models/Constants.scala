package models

import java.io.File
import com.typesafe.config.ConfigFactory


/**
  *
  * Created by lzimmermann on 29.05.16.
  */

// TODO This should not be a trait, because it already implements all of its members.
// TODO Rather, this should be a singleton object which dependent class get injected
trait Constants {

  val SEPARATOR: String = File.separator
  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEPARATOR"
  val serializedParam = "sparam"
  val nJobActors = 100
}

/** @author snam
  * case class handles ordering of param tabs
  * @param name: value for cli usage of parameters in the runscripts
  * @param inputType: 0 is NumberCompnent and 1 is SelectComponent
  * @param internalOrdering: allows to order the items within the params tab
  */

case class Param(name: String, inputType: Int, internalOrdering: Int)


trait ExitCodes {

  val SUCCESS = 0
  val TERMINATED = 143
}


/*
Param.ALIGNMENT -> alignmentFormats.map { format =>

format -> fullNames(format)
}.toSeq,
Param.STANDARD_DB -> tel.generateValues(Param.STANDARD_DB).toSeq,
Param.HHBLITSDB -> tel.generateValues(Param.HHBLITSDB).toSeq,
Param.HHSUITEDB -> tel.generateValues(Param.HHSUITEDB).toSeq,
Param.MSAGENERATION -> tel.generateValues(Param.MSAGENERATION).toSeq,
Param.MSA_GEN_MAX_ITER -> tel.generateValues(Param.MSA_GEN_MAX_ITER).toSeq.sortBy(_._1),
Param.GENETIC_CODE -> tel.generateValues(Param.GENETIC_CODE).toSeq,
Param.MATRIX -> matrixParams.map { matrix =>

matrix -> fullNames(matrix)
}.toSeq,
Param.EVAL_INC_THRESHOLD -> tel.generateValues(Param.EVAL_INC_THRESHOLD).toSeq.sortBy(_._1.toFloat),
Param.MIN_COV -> tel.generateValues(Param.MIN_COV).toSeq.sorted */

object Param {

  final val ALIGNMENT = Param("alignment",1,1)
  final val ALIGNMENT_FORMAT = Param("alignment_format",1,1)
  final val STANDARD_DB = Param("standarddb",1,1)
  final val HHSUITEDB = Param("hhsuitedb",1,1)
  final val MATRIX = Param("matrix",1,1)
  final val NUM_ITER = Param("num_iter",1,1)
  final val EVALUE = Param("evalue",1,1)
  final val GAP_OPEN = Param("gap_open",1,1)
  final val GAP_EXT = Param("gap_ext",1,1)
  final val GAP_TERM = Param("gap_term",1,1)
  final val DESC = Param("desc",1,1)
  final val CONSISTENCY = Param("consistency",1,1)
  final val ITREFINE = Param("itrefine",1,1)
  final val PRETRAIN = Param("pretrain",1,1)
  final val MAXROUNDS = Param("maxrounds",1,1)
  final val OFFSET = Param("offset",1,1)
  final val BONUSSCORE = Param("bonusscore",1,1)
  final val OUTORDER = Param("outorder",1,1)
  final val ETRESH = Param("inclusion_ethresh",1,1)
  final val HHBLITSDB  = Param("hhblitsdb",1,1)
  final val ALIGNMODE = Param("alignmode",1,1)
  final val MSAGENERATION = Param("msageneration",1,1)
  final val MSA_GEN_MAX_ITER = Param("msa_gen_max_iter",1,1)
  final val GENETIC_CODE = Param("genetic_code",1,1)
  final val LONG_SEQ_NAME = Param("long_seq_name",1,1)
  final val EVAL_INC_THRESHOLD = Param("inclusion_ethresh",1,1)
  final val MIN_COV = Param("min_cov",1,1)
  final val MAX_LINES = Param("max_lines",1,1)
  final val PMIN = Param("pmin",1,1)
  final val MAX_SEQS = Param("max_seqs",1,1)
  final val ALIWIDTH = Param("aliwidth",1,1)
  final val MAX_EVAL = Param("max_eval",0, 1)
  final val MAX_SEQID = Param("max_seqid", 0, 1)
  final val MIN_COLSCORE = Param("min_colscore", 0, 1)
  final val MIN_QUERY_COV = Param("min_query_cov", 0, 1)
  final val MIN_ANCHOR_WITH = Param("min_anchor_width", 0, 1)
  final val WEIGHTING = Param("weighting", 0, 1)
  final val RUN_PSIPRED = Param("run_psipred",0,1)
  final val MATRIX_PHYLIP = Param("matrix_phylip",0,1)
  final val MATRIX_PCOILS = Param("matrix_pcoils", 1, 1)
  final val PROTBLASTPROGRAM = Param("protblastprogram", 1, 1)
  final val FILTER_LOW_COMPLEXITY = Param("filter_low_complexity", 0, 1)
  final val MATRIX_MARCOIL = Param("matrix_marcoil", 1, 1)
  final val TRANSITION_PROBABILITY = Param("transition_probability", 1, 1)
  final val MIN_SEQID_QUERY = Param("min_seqid_query", 0, 1)
  final val NUM_SEQS_EXTRACT = Param("num_seqs_extract", 0, 1)
}