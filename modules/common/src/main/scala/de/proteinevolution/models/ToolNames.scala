package de.proteinevolution.models

object ToolNames {

  case class ToolName(value: String) extends AnyVal

  final val ALNVIZ              = ToolName("alnviz")
  final val REFORMAT            = ToolName("reformat")
  final val PSIBLAST            = ToolName("psiblast")
  final val CLANS               = ToolName("clans")
  final val TPRPRED             = ToolName("tprpred")
  final val HHBLITS             = ToolName("hhblits")
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
