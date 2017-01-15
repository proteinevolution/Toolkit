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
    Param.MIN_COV -> tel.generateValues(Param.MIN_COV).toSeq.sorted
  )
}

