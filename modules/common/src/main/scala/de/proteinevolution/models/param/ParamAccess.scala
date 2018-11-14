package de.proteinevolution.models.param
import de.proteinevolution.models.parameters.Parameter._
import de.proteinevolution.models.parameters._
import de.proteinevolution.tel.TEL
import javax.inject.{ Inject, Singleton }

/**
 * Provides the specification of the Parameters as they appear in the individual tools
 **/
@Singleton
class ParamAccess @Inject()(tel: TEL) {

  def select(name: String, label: String, maxSelectedOptions: Int = 1) =
    SelectParameter(name,
                    label,
                    tel.generateValues(name).toSeq.map(option => SelectOption(option._1, option._2)),
                    maxSelectedOptions)

  final val alignmentFormats = Seq(
    "fas" -> "fas",
    "a2m" -> "a2m",
    "a3m" -> "a3m",
    "sto" -> "sto",
    "psi" -> "psi",
    "clu" -> "clu"
  )

  def getParam(paramName: String, placeholder: String = "", sampleInputKey: String = ""): Parameter = paramName match {
    case "ALIGNMENT" => TextAreaParameter("alignment", TextAreaInputType.SEQUENCE, placeholder, sampleInputKey)
    case "TWOTEXTALIGNMENT" =>
      TextAreaParameter("alignment", TextAreaInputType.SEQUENCE, placeholder, sampleInputKey, allowsTwoTextAreas = true)
    case "HMMER_DB"                 => select("hmmerdb", "Select database")
    case "STANDARD_DB"              => select("standarddb", "Select standard database")
    case "HHSUITEDB"                => select("hhsuitedb", "Select database (PDB_mmCIF70 for modeling)")
    case "MATRIX"                   => select("matrix", "Scoring Matrix")
    case "EVALUE"                   => select("evalue", "E-value")
    case "GAP_OPEN"                 => NumberParameter("gap_open", "Gap open penalty")
    case "GAP_TERM"                 => NumberParameter("gap_term", "Terminal gap penalty", Some(0), Some(10), Some(0.01))
    case "GAP_EXT_KALN"             => NumberParameter("gap_ext_kaln", "Gap extension penalty", Some(0), Some(10), Some(0.01))
    case "BONUSSCORE"               => NumberParameter("bonusscore", "Bonus Score", Some(0), Some(10), Some(0.01))
    case "DESC"                     => select("desc", "No. of target sequences (up to 10000)")
    case "MAXROUNDS"                => select("maxrounds", "Max. number of iterations")
    case "OFFSET"                   => NumberParameter("offset", "Offset", Some(0), Some(10), Some(0.01))
    case "HHBLITSDB"                => select("hhblitsdb", "Select database")
    case "ALIGNMODE"                => select("alignmode", "Alignment Mode")
    case "MSA_GEN_MAX_ITER"         => select("msa_gen_max_iter", "Maximal no. of MSA generation steps")
    case "MSA_GEN_MAX_ITER_HHREPID" => select("msa_gen_max_iter_hhrepid", "Maximal no. of MSA generation steps")
    case "MSA_GEN_METHOD"           => select("msa_gen_method", "MSA generation method")
    case "INC_AMINO"                => select("inc_amino", "Include amino acid sequence in output")
    case "GENETIC_CODE"             => select("genetic_code", "Choose a genetic Code")
    case "MACMODE"                  => select("macmode", "Realign with MAC")
    case "MACTHRESHOLD"             => select("macthreshold", "MAC realignment threshold")
    case "MIN_COV"                  => NumberParameter("min_cov", "Min. coverage of MSA hits (%)", Some(0), Some(100))
    case "PMIN"                     => NumberParameter("pmin", "Min. probability in hit list (> 10%)", Some(0), Some(100))
    case "MAX_SEQID"                => NumberParameter("max_seqid", "Maximal Sequence Identity (%)")
    case "MIN_QUERY_COV"            => NumberParameter("min_query_cov", "Minimal coverage with query (%)", Some(0), Some(100))
    case "MATRIX_PHYML"             => select("matrix_phyml", "Model of AminoAcid replacement")
    case "MATRIX_MARCOIL"           => select("matrix_marcoil", "Matrix")
    case "TRANSITION_PROBABILITY"   => select("transition_probability", "Transition Probability")
    case "MIN_SEQID_QUERY" =>
      NumberParameter("min_seqid_query", "Min. seq. identity of MSA hits with query (%)", Some(0), Some(100))
    case "NUM_SEQS_EXTRACT" =>
      NumberParameter("num_seqs_extract", "No. of most dissimilar sequences to extract")
    case "SCORE_SS"                => select("score_ss", "Score secondary structure")
    case "SS_SCORING"              => select("ss_scoring", "Secondary structure scoring")
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
    case "OUTPUT_ORDER"            => select("output_order", "Output the alignment in:")
    case "EVAL_TPR"                => select("eval_tpr", "E-value inclusion TPR & SEL")
    case "CODON_TABLE_ORGANISM"    => TextInputParameter("codon_table_organism", "Use codon usage table of", "")
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
    case "REGKEY"                  => ModellerParameter("regkey", "Enter MODELLER-key (see help pages for details)")
    case "SAMCC_HELIXONE" =>
      TextInputParameter("samcc_helixone", "Definition for helix 1", "CC_first_position;chain;start_pos;end_pos")
    case "SAMCC_HELIXTWO" =>
      TextInputParameter("samcc_helixtwo", "Definition for helix 2", "CC_first_position;chain;start_pos;end_pos")
    case "SAMCC_HELIXTHREE" =>
      TextInputParameter("samcc_helixthree", "Definition for helix 3", "CC_first_position;chain;start_pos;end_pos")
    case "SAMCC_HELIXFOUR" =>
      TextInputParameter("samcc_helixfour", "Definition for helix 4", "CC_first_position;chain;start_pos;end_pos")
    case "INVOKE_PSIPRED" =>
      NumberParameter("invoke_psipred", "% identity cutoff to invoke a new PSIPRED run", Some(0), Some(100))
    case "CLANS_EVAL"       => select("clans_eval", "Extract BLAST HSP's up to E-values of")
    case "PATSEARCH_DB"     => select("patsearchdb", "Select database")
    case "MAFFT_GAP_OPEN"   => NumberParameter("mafft_gap_open", "Gap open penalty", Some(0), Some(10), Some(0.01))
    case "HHOMPDB"          => select("hhompdb", "Select HMM databases")
    case "QUICK_ITERS"      => select("quick_iters", "Maximal no. of MSA generation steps")
    case "TARGET_PSI_DB"    => select("target_psi_db", "Select database for MSA generation ")
    case "WINDOW_SIZE"      => NumberParameter("window_size", "Window size (< sequence length)")
    case "PERIODICITY_MIN"  => NumberParameter("periodicity_min", "Periodicity range - Min")
    case "PERIODICITY_MAX"  => NumberParameter("periodicity_max", "Periodicity range - Max")
    case "FTWIN_THRESHOLD"  => NumberParameter("ftwin_threshold", "FTwin threshold")
    case "REPWIN_THRESHOLD" => NumberParameter("repwin_threshold", "REPwin threshold")
    case "IN_FORMAT"        => select("in_format", "Input format")
    case "OUT_FORMAT"       => select("out_format", "Output format")
    case "CLUSTERING_MODE"  => select("clustering_mode", "Clustering mode")
  }

  val paramGroups = Map(
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
