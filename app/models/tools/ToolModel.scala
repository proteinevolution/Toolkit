package models.tools

import javax.inject.{Inject, Singleton}


// Returned to the View if a tool is requested with the getTool route
case class Toolitem(toolname : String,
                    toolnameLong : String,
                    toolnameAbbrev : String,
                    category : String,
                    optional : String,
                    params : Seq[(String, Seq[(String, Seq[(String, String)], ParamType, String)])])

// Specification of the internal representation of a Tool
case class Tool(toolNameShort: String,
                toolNameLong: String,
                toolNameAbbrev: String,
                category: String,
                optional: String,
                params: Map[String, Param],
                results: Seq[String],
                toolitem: Toolitem,
                paramGroups: Map[String, Seq[String]])


// Class which provides access to all Tools
@Singleton
final class ToolFactory @Inject() (paramAccess: ParamAccess) {

  // Contains the tool specifications and generates tool objects accordingly
  lazy val values : Map[String, Tool] = Set(
    // Protblast
    ("protblast", "ProtBlast", "prob", "search", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB, paramAccess.MATRIX, paramAccess.EVALUE,
        paramAccess.EVAL_INC_THRESHOLD, paramAccess.GAP_OPEN, paramAccess.GAP_EXT, paramAccess.DESC, paramAccess.PROTBLASTPROGRAM),
      Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")),

    // HHblits
  ("hhblits", "HHblits", "hhb", "search", "",
    Seq(paramAccess.ALIGNMENT,paramAccess.HHBLITSDB, paramAccess.MAXROUNDS),
    Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")),

    // HHpred
    ("hhpred", "HHpred", "hhp", "search", "",
    Seq(paramAccess.ALIGNMENT, paramAccess.HHSUITEDB, paramAccess.MSAGENERATION,
        paramAccess.MSA_GEN_MAX_ITER, paramAccess.MIN_COV, paramAccess.EVAL_INC_THRESHOLD,
        paramAccess.MAX_LINES, paramAccess.PMIN, paramAccess.ALIWIDTH),
      Seq("Hitlist", "FullAlignment")),

    // PSI-BLAST
    ("psiblast", "PSI-BLAST", "pbl", "search", "", Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB,
      paramAccess.MATRIX,
      paramAccess.NUM_ITER, paramAccess.EVALUE, paramAccess.EVAL_INC_THRESHOLD, paramAccess.GAP_OPEN,
      paramAccess.GAP_EXT, paramAccess.DESC),
      Seq("Hits", "E-Values", "Fasta", "AlignmentViewer")),

   // T-Coffee
    ("tcoffee", "T-Coffee", "tcf", "alignment", "", Seq(paramAccess.ALIGNMENT),
      Seq("Alignment", "AlignmentViewer", "Conservation", "Text")),

    // Blammer
    ("blammer", "Blammer", "blam", "alignment", "", Seq(paramAccess.ALIGNMENT,
      paramAccess.MIN_QUERY_COV, paramAccess.MAX_EVAL, paramAccess.MIN_ANCHOR_WITH,
      paramAccess.MAX_SEQID, paramAccess.MAX_SEQS, paramAccess.MIN_COLSCORE),
      Seq("Alignment", "AlignmentViewer")),

    // CLustalOmega
    ("clustalo", "Clustal Omega", "cluo", "alignment", "", Seq(paramAccess.ALIGNMENT),
      Seq("Alignment", "AlignmentViewer")),

    // MSA Probs
    ("msaprobs", "MSAProbs", "msap", "alignment", "", Seq(paramAccess.ALIGNMENT),Seq("Alignment", "AlignmentViewer")),

    // MUSCLE
    ("muscle", "MUSCLE", "musc", "alignment", "", Seq(paramAccess.ALIGNMENT, paramAccess.MAXROUNDS), Seq("Alignment", "AlignmentViewer")),

  // MAFFT
    ("mafft", "Mafft", "mft", "alignment", "", Seq(paramAccess.ALIGNMENT, paramAccess.GAP_OPEN, paramAccess.OFFSET),
      Seq("Alignment", "AlignmentViewer")),

   // Kalign
      ("kalign", "Kalign", "kal", "alignment", "",
        Seq(paramAccess.ALIGNMENT, paramAccess.GAP_OPEN, paramAccess.GAP_EXT, paramAccess.GAP_TERM, paramAccess.BONUSSCORE),
        Seq("Alignment", "AlignmentViewer")),

    // Hmmer
    ("hmmer", "HMMER", "hmmr", "search", "", Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB),
      Seq("fileview")),


    // Aln2Plot
    ("aln2plot", "Aln2Plot", "a2pl", "seqanal", "", Seq(paramAccess.ALIGNMENT),
      Seq("Hydrophobicity", "SideChainVolume")),

    // PCOILS
    ("pcoils", "PCOILS", "pco", "seqanal", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.WEIGHTING, paramAccess.MATRIX_PCOILS, paramAccess.RUN_PSIPRED),
      Seq.empty[String]),

    // FRrped
    ("frpred", "FRpred", "frp", "seqanal", "",Seq(paramAccess.ALIGNMENT), Seq.empty[String]),


    // HHrepID
    ("hhrepid", "HHrepid", "hhr", "seqanal", "",Seq(paramAccess.ALIGNMENT), Seq.empty[String]),


    // MARCOIL
    ("marcoil", "MARCOIL", "mar", "seqanal", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_MARCOIL, paramAccess.TRANSITION_PROBABILITY),
      Seq("CC-Prob", "ProbList/PSSM", "ProbState", "Domains")),

    // REPPER
    ("repper", "Repper", "rep", "seqanal", "",
      Seq(paramAccess.ALIGNMENT),
      Seq.empty[String]),

    // TPRpred
    ("tprpred", "TPRpred", "tprp", "seqanal", "",
      Seq(paramAccess.ALIGNMENT),
      Seq.empty[String]),

    // HHomp
    ("hhomp", "HHomp", "hho", "2ary", "",
      Seq(paramAccess.ALIGNMENT),
      Seq.empty[String]),

    // Quick 2D
    ("quick2d", "Quick2D", "q2d", "2ary", "",
      Seq(paramAccess.ALIGNMENT),
      Seq.empty[String]),

    // Ali2D
    ("ali2d", "Ali2D", "a2d", "2ary", "",
      Seq(paramAccess.ALIGNMENT),
      Seq.empty[String]),

    // Modeller
    ("modeller", "Modeller", "mod", "3ary", "",
      Seq(paramAccess.ALIGNMENT),
      Seq.empty[String]),

    // ANCESCON
    ("ancescon", "ANCESCON", "anc", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.LONG_SEQ_NAME),
      Seq("Tree")),

    // PHYLIP
    ("phylip", "PHYLIP-NEIGHBOR", "phyn", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_PHYLIP),
      Seq("NeighborJoining", "UPGMA")),

    // Backtranslate
    ("backtrans", "Backtranslator", "bac", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.GENETIC_CODE),
      Seq("DNA")),

    // HHfilter
    ("hhfilter", "HHFilter", "hhfi", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MAX_SEQID, paramAccess.MIN_SEQID_QUERY, paramAccess.MIN_QUERY_COV,
        paramAccess.NUM_SEQS_EXTRACT),
      Seq.empty)).map { t =>
    t._1  -> tool(t._1, t._2, t._3, t._4, t._5, t._6, t._7)
  }.toMap

   // Generates a new Tool object from the Tool specification
    def tool(toolNameShort: String,
             toolNameLong: String,
             toolNameAbbrev: String,
             category: String,
             optional: String,
             params: Seq[Param],
             results: Seq[String]) : Tool = {

            lazy val paramGroups = Map(
              "Input" -> Seq(paramAccess.ALIGNMENT.name, paramAccess.ALIGNMENT_FORMAT.name, paramAccess.STANDARD_DB.name, paramAccess.HHSUITEDB.name,
                paramAccess.PROTBLASTPROGRAM.name)
            )
            // Params which are not a part of any group (given by the name)
            lazy val remainParamName : String = "Parameters"
            val remainParams : Seq[String] = params.map(_.name).diff(paramGroups.values.flatten.toSeq)
            val paramMap = params.map(p => p.name -> p).toMap


            val toolitem = Toolitem(
              toolNameShort,
              toolNameLong,
              toolNameAbbrev,
              optional,
              category,
              // Constructs the Parameter specification such that the View can render the input fields
              paramGroups.keysIterator.map { group =>
                group ->  paramGroups(group).filter(params.map(_.name).contains(_)).map { param =>
                  (param, paramAccess.allowed.getOrElse(param, Nil), paramMap(param).paramType, paramMap(param).label)
                }
              }.toSeq :+
                remainParamName -> remainParams.map { param =>

                  (param, paramAccess.allowed.getOrElse(param, Nil), paramMap(param).paramType, paramMap(param).label)
                }
            )
            Tool(toolNameShort, toolNameLong, toolNameAbbrev, category,optional,paramMap,
              results, toolitem, paramGroups)
          }
}
