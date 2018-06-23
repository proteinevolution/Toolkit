package de.proteinevolution.models

import de.proteinevolution.models.ToolName.toolTitlesDictionary

import scala.collection.immutable

case class ToolName(value: String) extends AnyVal {
  override def toString = toolTitlesDictionary(value)
}

object ToolName {

  // case sensitive tool names used for title notifications
  private val toolTitlesDictionary: Map[String, String] = immutable
    .Map[String, String](
      "hhblits"   -> "HHblits",
      "hhpred"    -> "HHpred",
      "hmmer"     -> "HMMER",
      "patsearch" -> "PatternSearch",
      "psiblast"  -> "PSI-BLAST",
      "alnviz"    -> "AlignmentViewer",
      "clustalo"  -> "ClustalΩ",
      "deepcoil"  -> "DeepCoil",
      "kalign"    -> "Kalign",
      "mafft"     -> "MAFFT",
      "msaprobs"  -> "MSAProbs",
      "muscle"    -> "MUSCLE",
      "tcoffee"   -> "T-Coffee",
      "aln2plot"  -> "Aln2Plot",
      "hhrepid"   -> "HHrepID",
      "marcoil"   -> "MARCOIL",
      "pcoils"    -> "PCOILS",
      "repper"    -> "REPPER",
      "tprpred"   -> "TPRpred",
      "ali2d"     -> "Ali2D",
      "hhomp"     -> "HHomp",
      "quick2d"   -> "Quick2D",
      "modeller"  -> "MODELLER",
      "samcc"     -> "SamCC",
      "ancescon"  -> "ANCESCON",
      "clans"     -> "CLANS",
      "mmseqs2"   -> "MMseqs2",
      "phyml"     -> "PhyML",
      "sixframe"  -> "6Frame",
      "backtrans" -> "BackTranslator",
      "formatseq" -> "FormatSeq",
      "hhfilter"  -> "HHfilter",
      "retseq"    -> "RetrieveSeq",
      "seq2id"    -> "Seq2ID",
      "reformat"  -> "Reformat"
    )
    .withDefaultValue("")

  final val ALNVIZ              = ToolName("alnviz")
  final val REFORMAT            = ToolName("reformat")
  final val PSIBLAST            = ToolName("psiblast")
  final val CLANS               = ToolName("clans")
  final val TPRPRED             = ToolName("tprpred")
  final val HHBLITS             = ToolName("hhblits")
  final val DEEPCOIL             = ToolName("deepcoil")
  final val MARCOIL             = ToolName("marcoil")
  final val PCOILS              = ToolName("pcoils")
  final val REPPER              = ToolName("repper")
  final val MODELLER            = ToolName("modeller")
  final val HMMER               = ToolName("hmmer")
  final val HHPRED              = ToolName("hhpred")
  final val HHPRED_ALIGN        = ToolName("hhpred_align")
  final val HHPRED_MANUAL       = ToolName("hhpred_manual")
  final val HHREPID             = ToolName("hhrepid")
  final val ALI2D               = ToolName("ali2d")
  final val QUICK2D             = ToolName("quick2d")
  final val CLUSTALO            = ToolName("clustalo")
  final val KALIGN              = ToolName("kalign")
  final val MAFFT               = ToolName("mafft")
  final val MSAPROBS            = ToolName("msaprobs")
  final val MUSCLE              = ToolName("muscle")
  final val TCOFFEE             = ToolName("tcoffee")
  final val ALN2PLOT            = ToolName("aln2plot")
  final val ANCESCON            = ToolName("ancescon")
  final val PHYML               = ToolName("phyml")
  final val MMSEQS2             = ToolName("mmseqs2")
  final val RETSEQ              = ToolName("retseq")
  final val SEQ2ID              = ToolName("seq2id")
  final val SAMCC               = ToolName("samcc")
  final val SIXFRAMETRANSLATION = ToolName("sixframe")
  final val BACKTRANS           = ToolName("backtrans")
  final val HHFILTER            = ToolName("hhfilter")
  final val PATSEARCH           = ToolName("patsearch")
  final val HHOMP               = ToolName("hhomp")
  final val FORMATSEQ           = ToolName("formatseq")

}
