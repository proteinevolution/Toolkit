package models.tools

import javax.inject.{Inject, Singleton}


// Returned to the View if a tool is requested with the getTool route
case class Toolitem(toolname : String,
                    toolnameLong : String,
                    toolnameAbbrev : String,
                    category : String,
                    optional : String,
                    params : Seq[(String, Seq[(String, Seq[(String, String)])])])

// Specification of the internal representation of a Tool
case class Tool(toolNameShort: String,
                toolNameLong: String,
                toolNameAbbrev: String,
                category: String,
                optional: String,
                params: Seq[String],
                results: Seq[String],
                toolitem: Toolitem,
                paramGroups: Map[String, Seq[String]])


// Class which provides access to all Tools
@Singleton
class ToolFactory @Inject() (paramAccess: ParamAccess) {

  // Contains the tool specifications and generates tool objects accordingly
  val values : Map[String, Tool] = Set(
    // Protblast
    ("protblast", "ProtBlast", "prob", "search", "",
      Seq(paramAccess.ALIGNMENT.name, "standarddb", "matrix", "num_iter", "evalue", paramAccess.EVAL_INC_THRESHOLD.name, "gap_open", "gap_ext", "desc",
      paramAccess.PROTBLASTPROGRAM.name),
      Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")),

    // PatternSearch
    ("patternsearch", "PatternSearch", "pas", "search", "",
      Seq(paramAccess.ALIGNMENT.name, "standarddb", "matrix",
      "num_iter", "evalue", paramAccess.EVAL_INC_THRESHOLD.name, "gap_open", "gap_ext", "desc"),
      Seq("Hits", "E-Values", "Fasta", "AlignmentViewer") ),

    // HHblits
  ("hhblits", "HHblits", "hhb", "search", "",
    Seq(paramAccess.ALIGNMENT.name, "hhblitsdb", "maxrounds"),
    Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")),

    // HHpred
    ("hhpred", "HHpred", "hhp", "search", "",
    Seq(paramAccess.ALIGNMENT.name, paramAccess.HHSUITEDB.name, paramAccess.MSAGENERATION.name,
        paramAccess.MSA_GEN_MAX_ITER.name, paramAccess.MIN_COV.name, paramAccess.EVAL_INC_THRESHOLD.name,
        paramAccess.MAX_LINES.name, paramAccess.PMIN.name, paramAccess.ALIWIDTH.name),
      Seq("Hitlist", "FullAlignment")),

    // PSI-BLAST
    ("psiblast", "PSI-BLAST", "pbl", "search", "", Seq(paramAccess.ALIGNMENT.name, "standarddb", "matrix",
      "num_iter", "evalue", paramAccess.EVAL_INC_THRESHOLD.name, "gap_open", "gap_ext", "desc"),
      Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")),

   // T-Coffee
    ("tcoffee", "T-Coffee", "tcf", "alignment", "", Seq(paramAccess.ALIGNMENT.name),
      Seq("Alignment", "AlignmentViewer", "Conservation", "Text")),

    // Blammer
    ("blammer", "Blammer", "blam", "alignment", "", Seq(paramAccess.ALIGNMENT.name,
      paramAccess.MIN_QUERY_COV.name, paramAccess.MAX_EVAL.name, paramAccess.MIN_ANCHOR_WITH.name,
      paramAccess.MAX_SEQID.name, paramAccess.MAX_SEQS.name, paramAccess.MIN_COLSCORE.name),
      Seq("Alignment", "AlignmentViewer")),

    // CLustalOmega
    ("clustalo", "Clustal Omega", "cluo", "alignment", "", Seq(paramAccess.ALIGNMENT.name),
      Seq("Alignment", "AlignmentViewer")),

    // MSA Probs
    ("msaprobs", "MSAProbs", "msap", "alignment", "", Seq(paramAccess.ALIGNMENT.name),Seq("Alignment", "AlignmentViewer")),

    // MUSCLE
    ("muscle", "MUSCLE", "musc", "alignment", "", Seq("alignment", paramAccess.MAXROUNDS.name), Seq("Alignment", "AlignmentViewer")),

  // MAFFT
    ("mafft", "Mafft", "mft", "alignment", "", Seq(paramAccess.ALIGNMENT.name, paramAccess.GAP_OPEN.name, paramAccess.OFFSET.name),
      Seq("Alignment", "AlignmentViewer")),

   // Kalign
      ("kalign", "Kalign", "kal", "alignment", "",
        Seq(paramAccess.ALIGNMENT.name, paramAccess.GAP_OPEN.name, paramAccess.GAP_EXT.name, paramAccess.GAP_TERM.name, paramAccess.BONUSSCORE.name),
        Seq("Alignment", "AlignmentViewer")),

    // Hmmer
    ("hmmer", "HMMER", "hmmr", "search", "", Seq(paramAccess.ALIGNMENT.name, paramAccess.STANDARD_DB.name),
      Seq("fileview")),


    // Aln2Plot
    ("aln2plot", "Aln2Plot", "a2pl", "seqanal", "", Seq(paramAccess.ALIGNMENT.name),
      Seq("Hydrophobicity", "SideChainVolume")),

    // PCOILS
    ("pcoils", "PCOILS", "pco", "seqanal", "",
      Seq(paramAccess.ALIGNMENT.name, paramAccess.WEIGHTING.name, paramAccess.MATRIX_PCOILS.name, paramAccess.RUN_PSIPRED.name),
      Seq.empty[String]),

    // FRrped
    ("frpred", "FRpred", "frp", "seqanal", "",Seq(paramAccess.ALIGNMENT.name), Seq.empty[String]),


    // HHrepID
    ("hhrepid", "HHrepid", "hhr", "seqanal", "",Seq(paramAccess.ALIGNMENT.name), Seq.empty[String]),


    // MARCOIL
    ("marcoil", "MARCOIL", "mar", "seqanal", "",
      Seq(paramAccess.ALIGNMENT.name, paramAccess.MATRIX_MARCOIL.name, paramAccess.TRANSITION_PROBABILITY.name),
      Seq("CC-Prob", "ProbList/PSSM", "ProbState", "Domains")),

    // REPPER
    ("repper", "Repper", "rep", "seqanal", "",
      Seq(paramAccess.ALIGNMENT.name),
      Seq.empty[String]),

    // TPRpred
    ("tprpred", "TPRpred", "tprp", "seqanal", "",
      Seq(paramAccess.ALIGNMENT.name),
      Seq.empty[String]),

    // HHomp
    ("hhomp", "HHomp", "hho", "2ary", "",
      Seq(paramAccess.ALIGNMENT.name),
      Seq.empty[String]),

    // Quick 2D
    ("quick2d", "Quick2D", "q2d", "2ary", "",
      Seq(paramAccess.ALIGNMENT.name),
      Seq.empty[String]),

    // Ali2D
    ("ali2d", "Ali2D", "a2d", "2ary", "",
      Seq(paramAccess.ALIGNMENT.name),
      Seq.empty[String]),

    // Modeller
    ("modeller", "Modeller", "mod", "3ary", "",
      Seq(paramAccess.ALIGNMENT.name),
      Seq.empty[String]),

    // ANCESCON
    ("ancescon", "ANCESCON", "anc", "classification", "",
      Seq(paramAccess.ALIGNMENT.name, paramAccess.LONG_SEQ_NAME.name),
      Seq("Tree")),

    // PHYLIP
    ("phylip", "PHYLIP-NEIGHBOR", "phyn", "classification", "",
      Seq(paramAccess.ALIGNMENT.name, paramAccess.MATRIX_PHYLIP.name),
      Seq("NeighborJoining", "UPGMA")),

    // Backtranslate
    ("backtrans", "Backtranslator", "bac", "utils", "",
      Seq(paramAccess.ALIGNMENT.name, paramAccess.GENETIC_CODE.name),
      Seq("DNA")),

    // HHfilter
    ("hhfilter", "HHFilter", "hhfi", "utils", "",
      Seq(paramAccess.ALIGNMENT.name, paramAccess.MAX_SEQID.name, paramAccess.MIN_SEQID_QUERY.name, paramAccess.MIN_QUERY_COV.name,
        paramAccess.NUM_SEQS_EXTRACT.name),
      Seq.empty)).map { t =>
    t._1  -> tool(t._1, t._2, t._3, t._4, t._5, t._6, t._7)
  }.toMap

   // Generates a new Tool object from the Tool specification
    def tool(toolNameShort: String,
             toolNameLong: String,
             toolNameAbbrev: String,
             category: String,
             optional: String,
             params: Seq[String],
             results: Seq[String]): Tool = {

            val paramGroups = Map(
              "Input" -> Seq(paramAccess.ALIGNMENT.name, paramAccess.ALIGNMENT_FORMAT.name, paramAccess.STANDARD_DB.name, paramAccess.HHSUITEDB.name,
                paramAccess.PROTBLASTPROGRAM.name)
            )
            // Params which are not a part of any group
            val remainParamName : String = "Parameters"
            val remainParams : Seq[String] = params.diff(paramGroups.values.flatten.toSeq)

            val toolitem = Toolitem(
              toolNameShort,
              toolNameLong,
              toolNameAbbrev,
              optional,
              category,
              // Constructs the Parameter specification such that the View can render the input fields
              paramGroups.keysIterator.map { group =>
                group ->  paramGroups(group).filter(params.contains(_)).map { param =>
                  param -> paramAccess.allowed.getOrElse(param, Nil)
                }
              }.toSeq :+
                remainParamName -> remainParams.map { param =>

                  param -> paramAccess.allowed.getOrElse(param, Nil)
                }
            )
            Tool(toolNameShort, toolNameLong, toolNameAbbrev, category,optional,params, results, toolitem, paramGroups)
          }
}
