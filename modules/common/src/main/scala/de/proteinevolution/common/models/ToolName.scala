/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.common.models

import de.proteinevolution.common.models.ToolName._

import scala.collection.immutable

case class ToolName(value: String) extends AnyVal {

  override def toString: String = toolTitlesDictionary(value)

}

object ToolName {

  // case sensitive tool names used for title notifications
  private val toolTitlesDictionary: Map[String, String] = immutable
    .Map[String, String](
      "hhblits"           -> "HHblits",
      "hhpred"            -> "HHpred",
      "hmmer"             -> "HMMER",
      "patsearch"         -> "PatternSearch",
      "psiblast"          -> "PSI-BLAST",
      "alnviz"            -> "AlignmentViewer",
      "clustalo"          -> "ClustalΩ",
      "deepcoil"          -> "DeepCoil",
      "deepcoil2"         -> "DeepCoil2",
      "kalign"            -> "Kalign",
      "mafft"             -> "MAFFT",
      "msaprobs"          -> "MSAProbs",
      "muscle"            -> "MUSCLE",
      "tcoffee"           -> "T-Coffee",
      "aln2plot"          -> "Aln2Plot",
      "hhrepid"           -> "HHrepID",
      "marcoil"           -> "MARCOIL",
      "pcoils"            -> "PCOILS",
      "repper"            -> "REPPER",
      "tprpred"           -> "TPRpred",
      "ali2d"             -> "Ali2D",
      "hhomp"             -> "HHomp",
      "quick2d"           -> "Quick2D",
      "modeller"          -> "MODELLER",
      "samcc"             -> "SamCC",
      "ancescon"          -> "ANCESCON",
      "clans"             -> "CLANS",
      "mmseqs2"           -> "MMseqs2",
      "phyml"             -> "PhyML",
      "sixframe"          -> "6Frame",
      "backtrans"         -> "BackTranslator",
      "formatseq"         -> "FormatSeq",
      "hhfilter"          -> "HHfilter",
      "retseq"            -> "RetrieveSeq",
      "seq2id"            -> "Seq2ID",
      "reformat"          -> "Reformat",
      "plmblast"          -> "pLM-BLAST",
      "diamond_deepclust" -> "DIAMOND-DeepClust"
    )
    .withDefaultValue("")

  final val ALNVIZ              = ToolName("alnviz")
  final val REFORMAT            = ToolName("reformat")
  final val PSIBLAST            = ToolName("psiblast")
  final val CLANS               = ToolName("clans")
  final val TPRPRED             = ToolName("tprpred")
  final val HHBLITS             = ToolName("hhblits")
  final val DEEPCOIL            = ToolName("deepcoil")
  final val DEEPCOIL2           = ToolName("deepcoil2")
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
  final val PLMBLAST            = ToolName("plmblast")

}
