package de.proteinevolution.models.param

import de.proteinevolution.models.param.ParamType.{ Bool, Decimal, ModellerKey, Select, Sequence, Text }
import de.proteinevolution.tel.TEL
import javax.inject.{ Inject, Singleton }

/**
 * Provides the specification of the Parameters as they appear in the individual tools
 **/
@Singleton
class ParamAccess @Inject()(tel: TEL) {

  // TODO param intersection logic

  def select(name: String, label: String) = Param(name, Select(tel.generateValues(name).toSeq), 1, label)
  final val alignmentFormats = Seq(
    "fas" -> "fas",
    "a2m" -> "a2m",
    "a3m" -> "a3m",
    "sto" -> "sto",
    "psi" -> "psi",
    "clu" -> "clu"
  )

  def getParam(paramName: String, placeholder: String = ""): Param = paramName match {
    case "ALIGNMENT"        => Param("alignment", Sequence(alignmentFormats, placeholder, false), 1, "")
    case "TWOTEXTALIGNMENT" => Param("alignment", Sequence(alignmentFormats, placeholder, true), 1, "")
    case "HMMER_DB"         => select("hmmerdb", "Select database")
    case "STANDARD_DB"      => select("standarddb", "Select standard database")
    case "HHSUITEDB"        => select("hhsuitedb", "Select database (PDB_mmCIF70 for modeling)")
    case "MATRIX"           => select("matrix", "Scoring Matrix")
    case "NUM_ITER"         => Param("num_iter", ParamType.UnconstrainedNumber, 1, "No. of iterations")
    case "EVALUE"           => select("evalue", "E-value")
    case "GAP_OPEN"         => Param("gap_open", ParamType.UnconstrainedNumber, 1, "Gap open penalty")
    case "GAP_TERM"         => Param("gap_term", Decimal("0.01", Some(0), Some(10)), 1, "Terminal gap penalty")
    case "GAP_EXT_KALN"     => Param("gap_ext_kaln", Decimal("0.01", Some(0), Some(10)), 1, "Gap extension penalty")
    case "BONUSSCORE"       => Param("bonusscore", Decimal("0.01", Some(0), Some(10)), 1, "Bonus Score")
    case "DESC"             => select("desc", "No. of target sequences (up to 10000)")
    case "CONSISTENCY" =>
      Param("consistency", ParamType.UnconstrainedNumber, 1, "Passes of consistency transformation")
    case "ITREFINE"                 => Param("itrefine", ParamType.UnconstrainedNumber, 1, "Passes of iterative refinements")
    case "PRETRAIN"                 => Param("pretrain", ParamType.UnconstrainedNumber, 1, "Rounds of pretraining")
    case "MAXROUNDS"                => select("maxrounds", "Max. number of iterations")
    case "OFFSET"                   => Param("offset", Decimal("0.01", Some(0), Some(10)), 1, "Offset")
    case "OUTORDER"                 => Param("outorder", ParamType.UnconstrainedNumber, 1, "Outorder")
    case "ETRESH"                   => Param("inclusion_ethresh", ParamType.UnconstrainedNumber, 1, "E-value inclusion threshold")
    case "HHBLITSDB"                => Param("hhblitsdb", Select(tel.generateValues("hhblitsdb").toSeq), 1, "Select database")
    case "ALIGNMODE"                => select("alignmode", "Alignment Mode")
    case "MSA_GEN_MAX_ITER"         => select("msa_gen_max_iter", "Maximal no. of MSA generation steps")
    case "MSA_GEN_MAX_ITER_HHREPID" => select("msa_gen_max_iter_hhrepid", "Maximal no. of MSA generation steps")
    case "MSA_GEN_METHOD"           => select("msa_gen_method", "MSA generation method")
    case "INC_AMINO"                => select("inc_amino", "Include amino acid sequence in output")
    case "GENETIC_CODE"             => select("genetic_code", "Choose a genetic Code")
    case "LONG_SEQ_NAME"            => Param("long_seq_name", Bool, 1, "Use long sequence name")
    case "MACMODE"                  => select("macmode", "Realign with MAC")
    case "MACTHRESHOLD"             => select("macthreshold", "MAC realignment threshold")
    case "MIN_COV"                  => Param("min_cov", ParamType.Percentage, 1, "Min. coverage of MSA hits (%)")
    case "PMIN"                     => Param("pmin", ParamType.Percentage, 1, "Min. probability in hit list (> 10%)")
    case "MAX_SEQID"                => Param("max_seqid", ParamType.UnconstrainedNumber, 1, "Maximal Sequence Identity (%)")
    case "MIN_QUERY_COV"            => Param("min_query_cov", ParamType.Percentage, 1, "Minimal coverage with query (%)")
    case "MATRIX_PHYML"             => select("matrix_phyml", "Model of AminoAcid replacement")
    case "PROTBLASTPROGRAM"         => select("protblastprogram", "Program for Protein BLAST")
    case "FILTER_LOW_COMPLEXITY"    => Param("filter_low_complexity", Bool, 1, "Filter for low oltcomplexity regions")
    case "MATRIX_MARCOIL"           => select("matrix_marcoil", "Matrix")
    case "TRANSITION_PROBABILITY"   => select("transition_probability", "Transition Probability")
    case "MIN_SEQID_QUERY" =>
      Param("min_seqid_query", ParamType.Percentage, 1, "Min. seq. identity of MSA hits with query (%)")
    case "NUM_SEQS_EXTRACT" =>
      Param("num_seqs_extract", ParamType.UnconstrainedNumber, 1, "No. of most dissimilar sequences to extract")
    case "SCORE_SS"                => select("score_ss", "Score secondary structure")
    case "SS_SCORING"              => select("ss_scoring", "Secondary structure scoring")
    case "UNIQUE_SEQUENCE"         => select("unique_sequence", "Retrieve only unique sequences")
    case "MIN_SEQID"               => select("min_seqid", "Minimum sequence identity")
    case "MIN_ALN_COV"             => select("min_aln_cov", "Minimum alignment coverage")
    case "GRAMMAR"                 => select("grammar", "Select grammar")
    case "SEQCOUNT"                => select("seqcount", "Maximum number of sequences to display")
    case "INC_NUCL"                => select("inc_nucl", "Include nucleic acid sequence")
    case "AMINO_NUCL_REL"          => select("amino_nucl_rel", "Amino acids in relation to nucleotides")
    case "CODON_TABLE"             => select("codon_table", "Select codon usage table")
    case "MAX_HHBLITS_ITER"        => select("max_hhblits_iter", "MSA enrichment iterations using HHblits")
    case "PROTEOMES"               => select("proteomes", "Proteomes")
    case "REP_PVAL_THRESHOLD"      => select("rep_pval_threshold", "Repeat family P-value threshold")
    case "SELF_ALN_PVAL_THRESHOLD" => select("self_aln_pval_threshold", "Self-Alignment P-value threshold")
    case "MERGE_ITERS"             => select("merge_iters", "Merge rounds")
    case "DOMAIN_BOUND_DETECTION"  => select("domain_bound_detection", "Domain boundary detection")
    case "ALN_STRINGENCY"          => select("aln_stringency", "Alignment stringency")
    case "OUTPUT_ORDER"            => select("output_order", "Output the alignment in:")
    case "EVAL_TPR"                => select("eval_tpr", "E-value inclusion TPR & SEL")
    case "CODON_TABLE_ORGANISM"    => Param("codon_table_organism", Text(""), 1, "Use codon usage table of")
    case "HHPRED_INCL_EVAL"        => select("hhpred_incl_eval", "E-value incl. threshold for MSA generation")
    case "BLAST_INCL_EVAL"         => select("blast_incl_eval", "E-value inclusion threshold")
    case "HHBLITS_INCL_EVAL"       => select("hhblits_incl_eval", "E-value inclusion threshold")
    case "PCOILS_INPUT_MODE"       => select("pcoils_input_mode", "Input mode")
    case "REPPER_INPUT_MODE"       => select("repper_input_mode", "Input mode")
    case "PCOILS_WEIGHTING"        => select("pcoils_weighting", "Weighting")
    case "PCOILS_MATRIX"           => select("pcoils_matrix", "Matrix")
    case "NO_REPLICATES"           => select("no_replicates", "Number of replicates")
    case "SAMCC_PERIODICITY"       => select("samcc_periodicity", "Periodicity")
    case "EFF_CRICK_ANGLE"         => select("eff_crick_angle", "Effective Crick angle")
    case "REGKEY"                  => Param("regkey", ModellerKey, 1, "Enter MODELLER-key (see help pages for details)")
    case "SAMCC_HELIXONE" =>
      Param("samcc_helixone", Text("CC_first_position;chain;start_pos;end_pos"), 1, "Definition for helix 1")
    case "SAMCC_HELIXTWO" =>
      Param("samcc_helixtwo", Text("CC_first_position;chain;start_pos;end_pos"), 1, "Definition for helix 2")
    case "SAMCC_HELIXTHREE" =>
      Param("samcc_helixthree", Text("CC_first_position;chain;start_pos;end_pos"), 1, "Definition for helix 3")
    case "SAMCC_HELIXFOUR" =>
      Param("samcc_helixfour", Text("CC_first_position;chain;start_pos;end_pos"), 1, "Definition for helix 4")
    case "INVOKE_PSIPRED" =>
      Param("invoke_psipred", ParamType.Percentage, 1, "% identity cutoff to invoke a new PSIPRED run")
    case "CLANS_EVAL"       => select("clans_eval", "Extract BLAST HSP's up to E-values of")
    case "PATSEARCH_DB"     => select("patsearchdb", "Select database")
    case "MAFFT_GAP_OPEN"   => Param("mafft_gap_open", Decimal("0.01", Some(0), Some(10)), 1, "Gap open penalty")
    case "HHOMPDB"          => select("hhompdb", "Select HMM databases")
    case "QUICK_ITERS"      => select("quick_iters", "Maximal no. of MSA generation steps")
    case "TARGET_PSI_DB"    => select("target_psi_db", "Select database for MSA generation ")
    case "WINDOW_SIZE"      => Param("window_size", ParamType.UnconstrainedNumber, 1, "Window size (< sequence length)")
    case "PERIODICITY_MIN"  => Param("periodicity_min", ParamType.UnconstrainedNumber, 1, "Periodicity range - Min")
    case "PERIODICITY_MAX"  => Param("periodicity_max", ParamType.UnconstrainedNumber, 1, "Periodicity range - Max")
    case "FTWIN_THRESHOLD"  => Param("ftwin_threshold", ParamType.UnconstrainedNumber, 1, "FTwin threshold")
    case "REPWIN_THRESHOLD" => Param("repwin_threshold", ParamType.UnconstrainedNumber, 1, "REPwin threshold")
    case "IN_FORMAT"        => select("in_format", "Input format")
    case "OUT_FORMAT"       => select("out_format", "Output format")
    case "CLUSTERING_MODE"  => select("clustering_mode", "Clustering mode")
  }

  val paramGroups = Map(
    "Input" -> Seq(
      getParam("ALIGNMENT").name,
      getParam("STANDARD_DB").name,
      getParam("HHSUITEDB").name,
      getParam("PROTBLASTPROGRAM").name,
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
