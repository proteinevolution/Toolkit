package models

import javax.inject.{Inject, Singleton}
import modules.tel.TEL

/**
 *
 * Interfaces with TEL and provides access to the values which are allowed for generative Params
 *
 * Created by lzimmermann on 14.12.15.
 */
@Singleton
class Values @Inject() (tel : TEL) {

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
    Param.ALIGNMENT.name -> alignmentFormats.map { format =>

      format -> fullNames(format)
    }.toSeq,
    Param.STANDARD_DB.name -> tel.generateValues(Param.STANDARD_DB.name).toSeq,
    Param.HHBLITSDB.name -> tel.generateValues(Param.HHBLITSDB.name).toSeq,
    Param.HHSUITEDB.name -> tel.generateValues(Param.HHSUITEDB.name).toSeq,
    Param.MSAGENERATION.name -> tel.generateValues(Param.MSAGENERATION.name).toSeq,
    Param.MSA_GEN_MAX_ITER.name -> tel.generateValues(Param.MSA_GEN_MAX_ITER.name).toSeq.sortBy(_._1),
    Param.GENETIC_CODE.name -> tel.generateValues(Param.GENETIC_CODE.name).toSeq,
    Param.MATRIX.name -> matrixParams.map { matrix =>

      matrix -> fullNames(matrix)
    }.toSeq,
    Param.EVAL_INC_THRESHOLD.name -> tel.generateValues(Param.EVAL_INC_THRESHOLD.name).toSeq.sortBy(_._1.toFloat),
    Param.MIN_COV.name -> tel.generateValues(Param.MIN_COV.name).toSeq.sorted,
    Param.MATRIX_PHYLIP.name -> tel.generateValues(Param.MATRIX_PHYLIP.name).toSeq.sorted
  )
}

