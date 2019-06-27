/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
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

package de.proteinevolution.params

import de.proteinevolution.parameters.Parameter._
import de.proteinevolution.parameters.TextAreaInputType.TextAreaInputType
import de.proteinevolution.parameters._
import de.proteinevolution.tel.TEL
import javax.inject.{ Inject, Singleton }

/**
 * Provides the specification of the Parameters as they appear in the individual tools
 **/
@Singleton
class ParamAccess @Inject()(tel: TEL) {

  // TODO remove default default value
  def select(name: String, label: String, default: Option[String], maxSelectedOptions: Int = 1) =
    SelectParameter(
      name,
      label,
      default,
      tel.generateValues(name).toSeq.map(option => SelectOption(option._1, option._2)),
      maxSelectedOptions
    )

  final val alignmentFormats = Seq(
    "fas" -> "fas",
    "a2m" -> "a2m",
    "a3m" -> "a3m",
    "sto" -> "sto",
    "psi" -> "psi",
    "clu" -> "clu"
  )

  final val samCCHelixRegex: Option[String] = Some("^[a-r];[a-zA-Z0-9];\\d+;\\d+$")

  def getParam(
      paramName: String,
      placeholder: String = "",
      sampleInputKey: String = "",
      alignmentInputType: TextAreaInputType = TextAreaInputType.SEQUENCE
  ): Parameter = paramName match {

    // Common
    case "ALIGNMENT" => TextAreaParameter("alignment", alignmentInputType, placeholder, sampleInputKey)
    case "STANDARD_DB" => select("standarddb", "Select standard database", default = Some("nr50"))
    case "PMIN" => NumberParameter("pmin", "Min. probability in hit list (> 10%)", min = Some(0), max = Some(100), default = Some(20))
    case "DESC" => select("desc", "No. of target sequences (up to 10000)", default = Some("250"))
    case "MAXROUNDS" => select("maxrounds", "Max. number of iterations", default = Some("1"))
    case "EVALUE" => select("evalue", "E-value", default = Some("1e-3"))
    case "OUTPUT_ORDER" => select("output_order", "Output the alignment in:", default = Some("input"))
    case "PCOILS_INPUT_MODE" => select("pcoils_input_mode", "Input mode", default = Some("0"))
    case "MATRIX" => select("matrix", "Scoring Matrix", default = Some("BLOSUM62"))
    case "MIN_SEQID_QUERY" =>
      NumberParameter("min_seqid_query", "Min. seq. identity of MSA hits with query (%)", min = Some(0), max = Some(100), default = Some(0))

    // HHblits
    case "HHBLITSDB" => select("hhblitsdb", "Select database", default = Some("uniclust30_2018_08"))
    case "HHBLITS_INCL_EVAL" =>
      select("hhblits_incl_eval", "E-value inclusion threshold", default = Some("1e-3"))

    // HHpred
    case "TWOTEXTALIGNMENT" =>
      TextAreaParameter("alignment", alignmentInputType, placeholder, sampleInputKey, allowsTwoTextAreas = true)
    // TODO fix max selected options (the SUM of selected dbs from hhsuiteddb and proteomes must not be > 4)
    case "HHSUITEDB" => select("hhsuitedb", "Select database (PDB_mmCIF70 for modeling)", maxSelectedOptions = 4, default = Some("mmcif70/pdb70"))
    case "PROTEOMES" => select("proteomes", "Proteomes", maxSelectedOptions = 4, default = Some(""))
    case "MSA_GEN_METHOD" => select("msa_gen_method", "MSA generation method", default = Some("hhblits"))
    case "MSA_GEN_MAX_ITER" => select("msa_gen_max_iter", "Maximal no. of MSA generation steps", default = Some("3"))
    case "HHPRED_INCL_EVAL" => select("hhpred_incl_eval", "E-value incl. threshold for MSA generation", default = Some("1e-3"))
    case "MIN_COV" => NumberParameter("min_cov", "Min. coverage of MSA hits (%)", min = Some(0), max = Some(100), default = Some(20))
    case "SS_SCORING" => select("ss_scoring", "Secondary structure scoring", default = Some("2"))
    case "ALIGNMACMODE" => select("alignmacmode", "Alignment Mode/Realign with MAC", default = Some("local"))
    case "MACTHRESHOLD" => select("macthreshold", "MAC realignment threshold", default = Some("0.3"))

    // HMMER
    case "HMMER_DB" => select("hmmerdb", "Select database", default = Some("nr50"))
    case "MAX_HHBLITS_ITER" => select("max_hhblits_iter", "MSA enrichment iterations using HHblits", default = Some("1"))

    // PatternSearch
    case "PATSEARCH_DB" => select("patsearchdb", "Select database", default = Some("nr50"))
    case "GRAMMAR" => select("grammar", "Select grammar", default = Some("Prosite_grammar"))
    case "SEQCOUNT" => select("seqcount", "Maximum number of sequences to display", default = Some("500"))

    // PSI-BLAST
    case "BLAST_INCL_EVAL" =>
      select("blast_incl_eval", "E-value inclusion threshold", default = Some("1e-3"))

    // Kalign
    case "GAP_OPEN" => NumberParameter("gap_open", "Gap open penalty", default = Some(11))
    case "GAP_EXT_KALN" => NumberParameter("gap_ext_kaln", "Gap extension penalty", min = Some(0), max = Some(10), step = Some(0.01), default = Some(0.85))
    case "GAP_TERM" => NumberParameter("gap_term", "Terminal gap penalty", min = Some(0), max = Some(10), step = Some(0.01), default = Some(0.45))
    case "BONUSSCORE" => NumberParameter("bonusscore", "Bonus Score", min = Some(0), max = Some(10), step = Some(0.01), default = Some(0))

    // MAFFT
    case "MAFFT_GAP_OPEN" => NumberParameter("mafft_gap_open", "Gap open penalty", min = Some(0), max = Some(10), step = Some(0.01), default = Some(1.53))
    case "OFFSET" => NumberParameter("offset", "Offset", min = Some(0), max = Some(10), step = Some(0.01), default = Some(0.0))

    // HHrepID
    case "MSA_GEN_MAX_ITER_HHREPID" => select("msa_gen_max_iter_hhrepid", "Maximal no. of MSA generation steps", default = Some("3"))
    case "SCORE_SS" => select("score_ss", "Score secondary structure", default = Some("2"))
    case "REP_PVAL_THRESHOLD" => select("rep_pval_threshold", "Repeat family P-value threshold", default = Some("1e-2"))
    case "SELF_ALN_PVAL_THRESHOLD" => select("self_aln_pval_threshold", "Self-Alignment P-value threshold", default = Some("1e-1"))
    case "MERGE_ITERS" => select("merge_iters", "Merge rounds", default = Some("3"))
    case "DOMAIN_BOUND_DETECTION" => select("domain_bound_detection", "Domain boundary detection", default = Some("1"))

    // MARCOIL
    case "MATRIX_MARCOIL" => select("matrix_marcoil", "Matrix", default = Some("mtk"))
    case "TRANSITION_PROBABILITY" => select("transition_probability", "Transition Probability", default = Some("1"))

    // PCOILS
    case "PCOILS_WEIGHTING" => select("pcoils_weighting", "Weighting", default = Some("1"))
    case "PCOILS_MATRIX" => select("pcoils_matrix", "Matrix", default = Some("2"))

    // REPPER
    case "REPPER_INPUT_MODE" => select("repper_input_mode", "Input mode", default = Some("0"))
    case "WINDOW_SIZE" => NumberParameter("window_size", "Window size (< sequence length)", default = Some(100))
    case "PERIODICITY_MIN" => NumberParameter("periodicity_min", "Periodicity range - Min", default = Some(2))
    case "PERIODICITY_MAX" => NumberParameter("periodicity_max", "Periodicity range - Max", default = Some(100))
    case "FTWIN_THRESHOLD" => NumberParameter("ftwin_threshold", "FTwin threshold", default = Some(6))
    case "REPWIN_THRESHOLD" => NumberParameter("repwin_threshold", "REPwin threshold", default = Some(2))

    // TPRpred
    case "EVAL_TPR" => select("eval_tpr", "E-value inclusion TPR & SEL", default = Some("1e-2"))

    // Ali2D
    case "INVOKE_PSIPRED" =>
      NumberParameter("invoke_psipred", "% identity cutoff to invoke a new PSIPRED run", min = Some(0), max = Some(100), default = Some(30))

    // HHomp
    case "HHOMPDB" => select("hhompdb", "Select HMM databases", default = Some("HHompDB_v1.0"))
    case "ALIGNMODE" => select("alignmode", "Alignment Mode", default = Some("local"))

    // Quick2D
    case "TARGET_PSI_DB" => select("target_psi_db", "Select database for MSA generation", default = Some("nr90"))
    case "QUICK_ITERS" => select("quick_iters", "Maximal no. of MSA generation steps", default = Some("3"))

    // MODELLER
    case "REGKEY" => ModellerParameter("regkey", "Enter MODELLER-key (see help pages for details)")

    // SamCC
    case "SAMCC_HELIXONE" =>
      TextInputParameter(
        "samcc_helixone",
        "Definition for helix 1",
        "CC_first_position;chain;start_pos;end_pos",
        samCCHelixRegex,
        Some("a;A;2;30")
      )
    case "SAMCC_HELIXTWO" =>
      TextInputParameter(
        "samcc_helixtwo",
        "Definition for helix 2",
        "CC_first_position;chain;start_pos;end_pos",
        samCCHelixRegex,
        Some("a;B;2;30")
      )
    case "SAMCC_HELIXTHREE" =>
      TextInputParameter(
        "samcc_helixthree",
        "Definition for helix 3",
        "CC_first_position;chain;start_pos;end_pos",
        samCCHelixRegex,
        Some("a;C;2;30")
      )
    case "SAMCC_HELIXFOUR" =>
      TextInputParameter(
        "samcc_helixfour",
        "Definition for helix 4",
        "CC_first_position;chain;start_pos;end_pos",
        samCCHelixRegex,
        Some("a;D;2;30")
      )
    case "SAMCC_PERIODICITY" => select("samcc_periodicity", "Periodicity", default = Some("7"))
    case "EFF_CRICK_ANGLE" => select("eff_crick_angle", "Effective Crick angle", default = Some("1"))

    // CLANS
    case "CLANS_EVAL" => select("clans_eval", "Extract BLAST HSP's up to E-values of", default = Some("1e-4"))

    // MMseqs2
    case "MIN_SEQID" => select("min_seqid", "Minimum sequence identity", default = Some("0.8"))
    case "MIN_ALN_COV" => select("min_aln_cov", "Minimum alignment coverage", default = Some("0.8"))
    case "CLUSTERING_MODE" => select("clustering_mode", "Clustering mode", default = Some("cluster"))

    // PhyML
    case "MATRIX_PHYML" => select("matrix_phyml", "Model of AminoAcid replacement", default = Some("LG"))
    case "NO_REPLICATES" => select("no_replicates", "Number of replicates", default = Some("0"))

    // 6FrameTranslation
    case "INC_NUCL" => select("inc_nucl", "Include nucleic acid sequence", default = Some("t"))
    case "AMINO_NUCL_REL" => select("amino_nucl_rel", "Amino acids in relation to nucleotides", default = Some("t"))
    case "CODON_TABLE" => select("codon_table", "Select codon usage table", default = Some("1"))

    // BackTranslator
    case "INC_AMINO" => select("inc_amino", "Include amino acid sequence in output", default = Some("f"))
    case "GENETIC_CODE" => select("genetic_code", "Choose a genetic Code", default = Some("1"))
    case "CODON_TABLE_ORGANISM" => TextInputParameter("codon_table_organism", "Use codon usage table of", "")

    // FormatSeq
    case "IN_FORMAT" => select("in_format", "Input format", default = Some("a3m"))
    case "OUT_FORMAT" => select("out_format", "Output format", default = Some("fas"))

    // HHfilter
    case "MAX_SEQID" => NumberParameter("max_seqid", "Maximal Sequence Identity (%)", default = Some(90))
    case "MIN_QUERY_COV" => NumberParameter("min_query_cov", "Minimal coverage with query (%)", min = Some(0), max = Some(100), default = Some(0))
    case "NUM_SEQS_EXTRACT" =>
      NumberParameter("num_seqs_extract", "No. of most dissimilar sequences to extract", default = Some(0))
  }

  val paramGroups: Map[String, Seq[String]] = Map(
    "Input" -> Seq(
      getParam("ALIGNMENT").name,
      getParam("STANDARD_DB").name,
      getParam("HHSUITEDB").name,
      getParam("HHBLITSDB").name,
      getParam("HHOMPDB").name,
      getParam("PROTEOMES").name,
      getParam("HMMER_DB").name,
      getParam("PATSEARCH_DB").name,
      getParam("REGKEY").name,
      getParam("GRAMMAR").name,
      getParam("SAMCC_HELIXONE").name,
      getParam("SAMCC_HELIXTWO").name,
      getParam("SAMCC_HELIXTHREE").name,
      getParam("SAMCC_HELIXFOUR").name,
      getParam("TARGET_PSI_DB").name,
      getParam("QUICK_ITERS").name,
      getParam("PCOILS_INPUT_MODE").name,
      getParam("REPPER_INPUT_MODE").name,
      getParam("IN_FORMAT").name,
      getParam("OUT_FORMAT").name
    )
  )
}
