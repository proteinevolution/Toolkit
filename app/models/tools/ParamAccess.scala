package models.tools


import javax.inject.{Inject, Singleton}

import modules.tel.TEL


object Validators {

  // Validators
  def acceptAll(x: String) = true
}



/** @author snam
  * case class handles ordering of param tabs
  * @param name: value for cli usage of parameters in the runscripts
  * @param inputType: 0 is NumberCompnent and 1 is SelectComponent
  * @param internalOrdering: allows to order the items within the params tab
  */
case class Param(name: String, inputType: Int, internalOrdering: Int, validate: String => Boolean) {

  // Constructor for Parameter which accepts all arguments
  def this(name: String, inputType: Int, internalOrdering: Int) =
    this(name, inputType, internalOrdering, Validators.acceptAll)
}

/**
  * Provides the specification of the Parameters as they appear in the individual tools
  *
  * @param tel access to the allowedvalues of certain Parameters
  */
@Singleton
class ParamAccess @Inject() (tel: TEL) {

  // Shared parameters by all tools
  final val ALIGNMENT = new Param("alignment",1,1)
  final val ALIGNMENT_FORMAT =  new Param("alignment_format",1,1)
  final val STANDARD_DB = new Param("standarddb",1,1)
  final val HHSUITEDB = new Param("hhsuitedb",1,1)
  final val MATRIX = new Param("matrix",1,1)
  final val NUM_ITER = new Param("num_iter",1,1)
  final val EVALUE = new Param("evalue",1,1)
  final val GAP_OPEN = new Param("gap_open",1,1)
  final val GAP_EXT = new Param("gap_ext",1,1)
  final val GAP_TERM = new Param("gap_term",1,1)
  final val DESC = new Param("desc",1,1)
  final val CONSISTENCY =  new Param("consistency",1,1)
  final val ITREFINE = new Param("itrefine",1,1)
  final val PRETRAIN =  new Param("pretrain",1,1)
  final val MAXROUNDS = new Param("maxrounds",1,1)
  final val OFFSET = new Param("offset",1,1)
  final val BONUSSCORE = new Param("bonusscore",1,1)
  final val OUTORDER = new Param("outorder",1,1)
  final val ETRESH = new Param("inclusion_ethresh",1,1)
  final val HHBLITSDB  =  new Param("hhblitsdb",1,1)
  final val ALIGNMODE = new Param("alignmode",1,1)
  final val MSAGENERATION = new Param("msageneration",1,1)
  final val MSA_GEN_MAX_ITER = new Param("msa_gen_max_iter",1,1)
  final val GENETIC_CODE = new Param("genetic_code",1,1)
  final val LONG_SEQ_NAME = new Param("long_seq_name",1,1)
  final val EVAL_INC_THRESHOLD = new Param("inclusion_ethresh",1,1)
  final val MIN_COV = new Param("min_cov",1,1)
  final val MAX_LINES = new Param("max_lines",1,1)
  final val PMIN = new Param("pmin",1,1)
  final val MAX_SEQS = new Param("max_seqs",1,1)
  final val ALIWIDTH = new Param("aliwidth",1,1)
  final val MAX_EVAL = new Param("max_eval",0, 1)
  final val MAX_SEQID = new Param("max_seqid", 0, 1)
  final val MIN_COLSCORE = new Param("min_colscore", 0, 1)
  final val MIN_QUERY_COV = new Param("min_query_cov", 0, 1)
  final val MIN_ANCHOR_WITH = new Param("min_anchor_width", 0, 1)
  final val WEIGHTING = new Param("weighting", 0, 1)
  final val RUN_PSIPRED = new Param("run_psipred",0,1)
  final val MATRIX_PHYLIP = new Param("matrix_phylip",0,1)
  final val MATRIX_PCOILS = new Param("matrix_pcoils", 1, 1)
  final val PROTBLASTPROGRAM = new Param("protblastprogram", 1, 1)
  final val FILTER_LOW_COMPLEXITY =  new Param("filter_low_complexity", 0, 1)
  final val MATRIX_MARCOIL = new Param("matrix_marcoil", 1, 1)
  final val TRANSITION_PROBABILITY = new Param("transition_probability", 1, 1)
  final val MIN_SEQID_QUERY = new Param("min_seqid_query", 0, 1)
  final val NUM_SEQS_EXTRACT =  new Param("num_seqs_extract", 0, 1)

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
    TRANSITION_PROBABILITY.name -> tel.generateValues(TRANSITION_PROBABILITY.name).toSeq.sorted
  )
}
