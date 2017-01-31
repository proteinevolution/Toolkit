package models.tools


import javax.inject.{Inject, Singleton}

import modules.tel.TEL


/**
  * Provides the specification of the Parameters as they appear in the individual tools
  *
  * @param tel access to the allowedvalues of certain Parameters
  */
@Singleton
class ParamAccess @Inject() (tel: TEL) {

  /** @author snam
    * case class handles ordering of param tabs
    * @param name: value for cli usage of parameters in the runscripts
    * @param inputType: 0 is NumberCompnent and 1 is SelectComponent
    * @param internalOrdering: allows to order the items within the params tab
    */
  case class Param(name: String, inputType: Int, internalOrdering: Int)

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