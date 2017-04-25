package models.tools

import javax.inject.{Inject, Singleton}

import models.database.results.{HHBlits, HHPred, Hmmer, PSIBlast}

import scala.concurrent._
import ExecutionContext.Implicits.global
import modules.CommonModule
import play.modules.reactivemongo.ReactiveMongoApi
import play.twirl.api.Html

import scala.concurrent.Future


// Returned to the View if a tool is requested with the getTool route
case class Toolitem(toolname : String,
                    toolnameLong : String,
                    toolnameAbbrev : String,
                    category : String,
                    optional : String,
                    params : Seq[(String, Seq[Param])])

// Specification of the internal representation of a Tool
case class Tool(toolNameShort: String,
                toolNameLong: String,
                toolNameAbbrev: String,
                category: String,
                optional: String,
                params: Map[String, Param], // Maps a parameter name to the respective Param instance
                toolitem: Toolitem,
                paramGroups: Map[String, Seq[String]],
                forwardAlignment: Seq[String],
                forwardMultiSeq: Seq[String]) {
  def isToolName (toolName : String, caseSensitive : Boolean = false): Boolean = {
    if (caseSensitive) {
      toolNameAbbrev.contains(toolName) || toolNameShort.contains(toolName) || toolNameLong.contains(toolName)
    } else {
      toolNameAbbrev.toLowerCase.contains(toolName.toLowerCase) ||
      toolNameShort.toLowerCase.contains(toolName.toLowerCase)  ||
      toolNameLong.toLowerCase.contains(toolName.toLowerCase)
    }
  }
}

// Class which provides access to all Tools
@Singleton
final class ToolFactory @Inject()(psi: PSIBlast, hmmer: Hmmer, hhpred: HHPred, hhblits: HHBlits) (paramAccess: ParamAccess, val reactiveMongoApi: ReactiveMongoApi) extends CommonModule{


  // Encompasses all the toolnames
  object Toolnames {

    final val PSIBLAST = "psiblast"
    final val CLANS = "clans"
    final val TPRPRED = "tprpred"
    final val HHBLITS = "hhblits"
    final val MARCOIL = "marcoil"
    final val PCOILS = "pcoils"
    final val MODELLER = "modeller"
    final val HMMER = "hmmer"
    final val HHPRED = "hhpred"
    final val HHPRED_ALIGN = "hhpred_align"
    final val HHPRED_MANUAL = "hhpred_manual"
    final val HHPRED_AUTOMATIC = "hhpred_automatic"
    final val HHREPID = "hhrepid"
    final val ALI2D = "ali2d"
    final val CLUSTALO = "clustalo"
    final val KALIGN = "kalign"
    final val MAFFT = "mafft"
    final val MSAPROBS = "msaprobs"
    final val MUSCLE = "muscle"
    final val TCOFFEE = "tcoffee"
    final val ALN2PLOT = "aln2plot"
    final val ANCESCON = "ancescon"
    final val PHYML = "phyml"
    final val MMSEQS2 = "mmseqs2"
    final val RETSEQ = "retseq"
    final val SEQ2ID = "seq2id"
    final val SAMCC = "samcc"
    final val SIXFRAMETRANSLATION = "6frametranslation"
    final val BACKTRANS = "backtrans"
    final val HHFILTER = "hhfilter"
    final val PATSEARCH = "patsearch"
  }

  // Encompasses some shared views of the result pages
  object Resultviews {

    final val HITLIST = "Hitlist"
    final val RESULTS = "Results"
    final val ALIGNMENT = "Alignment"
    final val ALIGNMENTVIEWER = "AlignmentViewer"
    final val TREE = "Tree"
    final val SUMMARY = "Summary"
    final val DATA = "Data"
  }



  // Contains the tool specifications and generates tool objects accordingly
  val values : Map[String, Tool] = Set(
    // HHblits
    ("hhblits", "HHblits", "hhb", "search", "",

    Seq(paramAccess.SEQORALI,paramAccess.HHBLITSDB, paramAccess.HHBLITS_INCL_EVAL, paramAccess.MAXROUNDS,
      paramAccess.PMIN, paramAccess.DESC), Seq("hhblits", "hhpred", "hhrepid" ),Seq("hhpred")),

    // HHpred
    ("hhpred", "HHpred", "hhp", "search", "",
      Seq(paramAccess.PROTEOMES, paramAccess.HHSUITEDB, paramAccess.TWOTEXTALIGNMENT, paramAccess.MSA_GEN_METHOD,
        paramAccess.MSA_GEN_MAX_ITER, paramAccess.SS_SCORING, paramAccess.MACMODE, paramAccess.MACTHRESHOLD,
        paramAccess.MIN_COV, paramAccess.MIN_SEQID_QUERY, paramAccess.HHPRED_INCL_EVAL,
        paramAccess.DESC, paramAccess.PMIN, paramAccess.ALIGNMODE), Seq("hhblits", "hhpred", "hhrepid"),Seq.empty),

    // HHpred - Manual Template Selection
    ("hhpred_manual", "HHpred - ManualTemplate Selection", "hhp", "forward", "",  Seq.empty, Seq.empty,Seq.empty),

    // HHpred - Manual Template Selection
    ("hhpred_automatic", "HHpred - Automatic Template Selection", "hhp", "forward", "",  Seq.empty, Seq.empty,Seq.empty),

    // PSI-BLAST
    ("psiblast", "ProtBLAST/PSI-BLAST", "pbl", "search", "", Seq(paramAccess.SEQORALI, paramAccess.STANDARD_DB,
      paramAccess.MATRIX,
      paramAccess.NUM_ITER, paramAccess.EVALUE, paramAccess.HHPRED_INCL_EVAL, paramAccess.DESC),
      Seq("psiblast", "hhpred", "hhblits", "hmmer", "clustalo", "kalign", "tcoffee", "mafft", "msaprobs", "muscle",
        "aln2plot", "pcoils", "hhrepid", "seq2id", "clans", "mmseqs2", "hhfilter"), Seq("clans", "mmseqs2", "seq2id")),


    // CLustalOmega
    ("clustalo", "Clustal Omega", "cluo", "alignment", "", Seq(paramAccess.ALIGNMENT,
      paramAccess.OUTPUT_ORDER), Seq("psiblast", "kalign", "tcoffee", "mafft", "msaprobs", "muscle", "hhpred", "hhblits", "hmmer", "hhfilter"),Seq.empty),

    // Kalign
    ("kalign", "Kalign", "kal", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER,
      paramAccess.GAP_OPEN, paramAccess.GAP_EXT_KALN, paramAccess.GAP_TERM, paramAccess.BONUSSCORE),
      Seq("psiblast", "clustalo", "tcoffee", "mafft", "msaprobs", "muscle", "hhpred", "hhblits", "hmmer", "hhfilter"),Seq.empty),

    // T-Coffee
    ("tcoffee", "T-Coffee", "tcf", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER),
      Seq("psiblast", "clustalo", "kalign", "mafft", "msaprobs", "muscle", "hhpred", "hhblits", "hmmer", "hhfilter"),Seq.empty),


    // MAFFT
    ("mafft", "MAFFT", "mft", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER, paramAccess.GAP_OPEN,
      paramAccess.OFFSET), Seq("psiblast", "clustalo", "kalign", "tcoffee", "msaprobs", "muscle", "hhpred", "hhblits", "hmmer", "hhfilter"),Seq.empty),


    // MSA Probs
    ("msaprobs", "MSAProbs", "msap", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER),
      Seq("psiblast", "clustalo", "kalign", "tcoffee", "mafft", "muscle", "hhpred", "hhblits", "hmmer", "hhfilter"),Seq.empty),

    // MUSCLE
    ("muscle", "MUSCLE", "musc", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER, paramAccess.MAXROUNDS),
      Seq("psiblast", "clustalo", "kalign", "tcoffee", "mafft", "msaprobs", "hhpred", "hhblits", "hmmer", "hhfilter"),Seq.empty),


    // Hmmer
    ("hmmer", "HMMER", "hmmr", "search", "", Seq(paramAccess.SEQORALI, paramAccess.HMMER_DB,
      paramAccess.MAX_HHBLITS_ITER, paramAccess.EVALUE, paramAccess.DESC), Seq("kalign"),Seq("hhblits")),


    // Aln2Plot
    ("aln2plot", "Aln2Plot", "a2pl", "seqanal", "", Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // PCOILS
    ("pcoils", "PCOILS", "pco", "seqanal", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.PCOILS_INPUT_MODE, paramAccess.PCOILS_MATRIX, paramAccess.PCOILS_WEIGHTING), Seq.empty,Seq.empty),

    // FRpred; Not for first release
    //("frpred", "FRpred", "frp", "seqanal", "",Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // HHrepID
    ("hhrepid", "HHrepID", "hhr", "seqanal", "",Seq(paramAccess.SEQORALI, paramAccess.MSA_GEN_MAX_ITER,
      paramAccess.SCORE_SS, paramAccess.REP_PVAL_THRESHOLD, paramAccess.SELF_ALN_PVAL_THRESHOLD, paramAccess.MERGE_ITERS,
      paramAccess.MAC_CUTOFF, paramAccess.ALN_STRINGENCY, paramAccess.DOMAIN_BOUND_DETECTION), Seq.empty,Seq.empty),

    // MARCOIL
    ("marcoil", "MARCOIL", "mar", "seqanal", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_MARCOIL, paramAccess.TRANSITION_PROBABILITY), Seq.empty,Seq.empty),

    // REPPER Not for first release
    //("repper", "Repper", "rep", "seqanal", "", Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // TPRpred
    ("tprpred", "TPRpred", "tprp", "seqanal", "",
      Seq(paramAccess.SINGLESEQ, paramAccess.EVAL_TPR), Seq.empty,Seq.empty),


    // Quick 2D
    ("quick2d", "Quick2D", "q2d", "2ary", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // Ali2D
    ("ali2d", "Ali2D", "a2d", "2ary", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // Modeller
    ("modeller", "MODELLER", "mod", "3ary", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.REGKEY), Seq.empty,Seq.empty),

    // SamCC
    ("samcc", "SamCC", "sam", "3ary", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.SAMCC_HELIXONE, paramAccess.SAMCC_HELIXTWO, paramAccess.SAMCC_HELIXTHREE,
        paramAccess.SAMCC_HELIXFOUR, paramAccess.SAMCC_PERIODICITY, paramAccess.EFF_CRICK_ANGLE), Seq.empty,Seq.empty),

    // RetrieveSeq
    ("retseq", "RetrieveSeq", "ret", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB, paramAccess.UNIQUE_SEQUENCE), Seq.empty,Seq.empty),

    // Seq2ID
    ("seq2id", "Seq2ID", "s2id", "utils", "",
      Seq(paramAccess.FASTAHEADERS), Seq("retseq"),Seq.empty),

    // ANCESCON
    ("ancescon", "ANCESCON", "anc", "classification", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // CLANS
    ("clans", "CLANS", "clan", "classification", "",
      Seq(paramAccess.MULTISEQ, paramAccess.MATRIX),
      Seq.empty,Seq.empty),

    // PhyML
    ("phyml", "PhyML", "phym", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_PHYML, paramAccess.NO_REPLICATES), Seq.empty,Seq.empty),

    // MMseqs2
    ("mmseqs2", "MMseqs2", "mseq", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MIN_SEQID, paramAccess.MIN_ALN_COV), Seq.empty,Seq.empty),

    // Backtranslator
    ("backtrans", "BackTranslator", "bac", "utils", "",
      Seq(paramAccess.SINGLESEQ, paramAccess.INC_AMINO, paramAccess.GENETIC_CODE, paramAccess.CODON_TABLE_ORGANISM), Seq.empty,Seq.empty),

    // PatternSearch
    ("patsearch", "PatternSearch", "pats", "search", "",
      Seq(paramAccess.MULTISEQ, paramAccess.STANDARD_DB, paramAccess.GRAMMAR, paramAccess.SEQCOUNT), Seq.empty,Seq.empty),

    // 6FrameTranslation
    ("6frametranslation", "6FrameTranslation", "6frt", "utils", "",
      Seq(paramAccess.SINGLESEQDNA, paramAccess.INC_NUCL, paramAccess.AMINO_NUCL_REL, paramAccess.CODON_TABLE), Seq.empty,Seq.empty),


    // HHfilter
    ("hhfilter", "HHfilter", "hhfi", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MAX_SEQID, paramAccess.MIN_SEQID_QUERY, paramAccess.MIN_QUERY_COV,
        paramAccess.NUM_SEQS_EXTRACT), Seq("hhblits", "hhpred", "hmmer", "psiblast", "clustalo", "kalign", "mafft", "msaprobs",
      "muscle", "tcoffee", "aln2plot", "hhrepid", "pcoils", "hhfilter"),Seq.empty)).map { t =>
    t._1  -> tool(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8)
  }.toMap



  // Maps toolname and resultpanel name to the function which transfers jobID and jobPath to an appropriate view
val resultMap : Map[String, Map[String, Function3[String, String,  play.api.mvc.RequestHeader,  Future[Html]]]  ] = Map(

  Toolnames.PSIBLAST -> Map(

    Resultviews.HITLIST -> { (_, jobID, requestHeader) =>

      getResult(jobID).map {

        case Some(jsvalue) =>
          implicit val  r = requestHeader
          views.html.jobs.resultpanels.psiblast.hitlist(jobID, psi.parseResult(jsvalue), this.values("psiblast"))
      }
    },
    "E-values" ->  { (_, jobID, requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.evalues(jobID)) }
  ),
  Toolnames.CLANS -> Map(

      Resultviews.RESULTS -> { (_, jobID,requestHeader) =>
        implicit val r = requestHeader
        Future.successful(views.html.jobs.resultpanels.clans("CLANS", jobID)) }
  ),
  Toolnames.TPRPRED -> Map(

    Resultviews.RESULTS -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
      case Some(jsvalue) =>
        implicit val r = requestHeader
        views.html.jobs.resultpanels.tprpred("TPRpred",jobID, jsvalue)}}
  ),
  Toolnames.HHBLITS -> Map(

    Resultviews.HITLIST -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
      case Some(jsvalue) =>
        implicit val r = requestHeader
        views.html.jobs.resultpanels.hhblits.hitlist(jobID, hhblits.parseResult(jsvalue), this.values(Toolnames.HHBLITS))}},

    "Representative_Alignment" -> { (_, jobID,requestHeader) => getResult(jobID).map {
      case Some(jsvalue) =>
        implicit val r = requestHeader
        views.html.jobs.resultpanels.alignment(jobID, jsvalue, "rep100" ,this.values(Toolnames.HHBLITS))}}
  ),

  Toolnames.MARCOIL -> Map(

    "CC-Prob" ->  { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.image(s"/files/$jobID/alignment_ncoils.png")) },
    "ProbState" ->  { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.ProbPerState")) },
    "Domains" -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.Domains")) },
    "ProbList/PSSM" -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.ProbList")) }
  ),
  Toolnames.PCOILS -> Map(

    "CC-Prob" -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.image(s"/files/$jobID/" + jobID + "_ncoils.png"))},
    "ProbList" -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/" + jobID + ".numerical"))}
  ),
  Toolnames.MODELLER -> Map(

    "3D-Structure" ->  { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.NGL3DStructure(s"/files/$jobID/$jobID.pdb", jobID + ".pdb", jobID, "Modeller"))},
    "VERIFY3D" ->  { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.verify3d.png", s"$jobPath$jobID/results/verify3d/$jobID.plotdat"))},
    "SOLVX" -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.solvx.png", s"$jobPath$jobID/results/solvx/$jobID.solvx"))},
    "ANOLEA" ->  { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.anolea.png", s"$jobPath$jobID/results/$jobID.pdb.profile"))}
  ),
  Toolnames.HMMER -> Map(

      Resultviews.HITLIST ->  { (_, jobID,requestHeader) =>
        implicit val r = requestHeader
        getResult(jobID).map {
          case Some(jsvalue) =>
            implicit val r = requestHeader
            views.html.jobs.resultpanels.hmmer.hitlist(jobID, hmmer.parseResult(jsvalue), this.values(Toolnames.HMMER))
        }}
  ),
  Toolnames.HHPRED -> Map(

    Resultviews.HITLIST ->  { (_ , jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.hhpred.hitlist(jobID, hhpred.parseResult(jsvalue), this.values(Toolnames.HHPRED))
      }},
    "Representative_Alignment" -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.alignment(jobID, jsvalue, "reduced" ,this.values(Toolnames.HHPRED))
      }}
  ),
  Toolnames.HHPRED_ALIGN -> Map(

    Resultviews.HITLIST -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.hhpred.hitlist(jobID, hhpred.parseResult(jsvalue), this.values(Toolnames.HHPRED_ALIGN))
      }},
    "FullAlignment" -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.msaviewer(jobID)) }
  ),
  Toolnames.HHPRED_MANUAL -> Map(

    Resultviews.RESULTS -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/results.out")) },
    "PIR" -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.hhpred.forward(s"$jobPath$jobID/results/tomodel.pir", jobID)) }
  ),
  Toolnames.HHPRED_AUTOMATIC -> Map(

    Resultviews.RESULTS ->  { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/out.hhr"))}
  ),
  Toolnames.HHREPID -> Map(

    Resultviews.RESULTS -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.modeller(s"/files/$jobID/query_A.png", s"$jobPath$jobID/results/query.hhrepid"))}
  ),
  Toolnames.ALI2D -> Map(

    Resultviews.DATA -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".aln",s"$jobPath$jobID/results/" + jobID + ".aln", jobID, "ali2d"))}
  ),
  Toolnames.CLUSTALO -> Map(

    Resultviews.ALIGNMENT -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.alignment(jobID, jsvalue , "alignment", this.values(Toolnames.CLUSTALO))
      }},
    Resultviews.ALIGNMENTVIEWER -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.msaviewer(jobID))}
  ),
  Toolnames.KALIGN -> Map(

    Resultviews.ALIGNMENT -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) =>  views.html.jobs.resultpanels.alignment(jobID, jsvalue, "alignment", this.values(Toolnames.KALIGN))
      }},
    Resultviews.ALIGNMENTVIEWER -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.msaviewer(jobID))}
  ),
  Toolnames.MAFFT -> Map(

    Resultviews.ALIGNMENT -> { (_, jobID,requestHeader) =>
      getResult(jobID).map {
        case Some(jsvalue) =>
          implicit val r = requestHeader
          views.html.jobs.resultpanels.alignment(jobID, jsvalue, "alignment", this.values(Toolnames.MAFFT))
      }},
    Resultviews.ALIGNMENTVIEWER ->  { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.msaviewer(jobID))}
  ),
  Toolnames.MSAPROBS -> Map(

    Resultviews.ALIGNMENT -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.alignment(jobID, jsvalue, "alignment", this.values(Toolnames.MSAPROBS))
      }},
    Resultviews.ALIGNMENTVIEWER -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.msaviewer(jobID))}
  ),
  Toolnames.MUSCLE -> Map(

    Resultviews.ALIGNMENT -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.alignment(jobID, jsvalue, "alignment", this.values(Toolnames.MUSCLE))
      }},
    Resultviews.ALIGNMENTVIEWER -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.msaviewer(jobID))}
  ),
  Toolnames.TCOFFEE -> Map(

    Resultviews.ALIGNMENT -> { (_, jobID,requestHeader) =>
      getResult(jobID).map {
        case Some(jsvalue) =>
          implicit val r = requestHeader
          views.html.jobs.resultpanels.alignment(jobID, jsvalue, "alignment", this.values(Toolnames.TCOFFEE))
      }},
    Resultviews.ALIGNMENTVIEWER -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.msaviewer(jobID))}
  ),
  Toolnames.ALN2PLOT -> Map(

    "Plots" -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.aln2plot(jobID))}
  ),
  Toolnames.ANCESCON -> Map(

    Resultviews.TREE -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.tree(jobID + ".clu.tre",s"$jobPath$jobID/results/" + jobID + ".clu.tre", jobID, "ancescon_output_tree"))},
    Resultviews.DATA -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(  views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".anc_out",s"$jobPath$jobID/results/" + jobID + ".anc_out", jobID, "ancescon_output_data"))}
  ),
  Toolnames.PHYML -> Map(

    Resultviews.TREE -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(  views.html.jobs.resultpanels.tree(jobID + ".phy_phyml_tree.txt",s"$jobPath$jobID/results/" + jobID + ".phy_phyml_tree.txt", jobID, "phyml_tree"))},
    Resultviews.DATA -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(  views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".stats",s"$jobPath$jobID/results/" + jobID + ".stats", jobID, "phyml_data"))}
  ),
  Toolnames.MMSEQS2 -> Map(

    Resultviews.RESULTS -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".fas",s"$jobPath$jobID/results/" + jobID + ".fas", jobID, "mmseqs_reps"))},
    Resultviews.SUMMARY -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".clu",s"$jobPath$jobID/results/" + jobID + ".clu", jobID, "mmseqs_clusters"))}
  ),
  Toolnames.RETSEQ -> Map(

    Resultviews.RESULTS -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/sequences.fa"))},
    Resultviews.SUMMARY -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/unretrievable"))}
  ),
  Toolnames.SEQ2ID -> Map(

    Resultviews.RESULTS -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.unchecked_list("Seq2ID",jobID, jsvalue)
      }}
  ),
  Toolnames.SAMCC -> Map(

    "3D-Structure-With-Axes" -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.NGL3DStructure(s"/files/$jobID/$jobID.pdb", jobID + ".pdb", jobID, "samcc_PDB_AXES"))},
    "Plots" -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.samcc(s"/files/$jobID/out0.png", s"/files/$jobID/out1.png", s"/files/$jobID/out2.png", s"/files/$jobID/out3.png"))},
    "NumericalData" -> { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".out",s"$jobPath$jobID/results/" + jobID + ".out", jobID, "samcc"))}
  ),
  Toolnames.SIXFRAMETRANSLATION -> Map(

    Resultviews.RESULTS ->  { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".out",s"$jobPath$jobID/results/" + jobID + ".out", jobID, "sixframetrans_out"))}
  ),
  Toolnames.BACKTRANS -> Map(

    Resultviews.RESULTS ->  { (jobPath, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful(views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".out",s"$jobPath$jobID/results/" + jobID + ".out", jobID, "backtrans"))}
  ),
  Toolnames.HHFILTER -> Map(

    Resultviews.ALIGNMENT -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.alignment(jobID, jsvalue, "alignment", this.values(Toolnames.HHFILTER))
      }},
    Resultviews.ALIGNMENTVIEWER -> { (_, jobID,requestHeader) =>
      implicit val r = requestHeader
      Future.successful( views.html.jobs.resultpanels.msaviewer(jobID))}
  ),
  Toolnames.PATSEARCH -> Map (

    "PatternSearch" -> { (_, jobID, requestHeader) =>
      implicit val  r = requestHeader
      getResult(jobID).map {
        case Some(jsvalue) => views.html.jobs.resultpanels.patternSearch("PatternSearch",jobID, "output", jsvalue)
      }}
  )
)

  // Encompasses the names of the resultviews for each tool
  val resultPanels : Map[String, Seq[String]] = this.resultMap.map { trp => trp._1 -> trp._2.keys.toSeq }


  // Generates a new Tool object from the Tool specification
    def tool(toolNameShort: String,
             toolNameLong: String,
             toolNameAbbrev: String,
             category: String,
             optional: String,
             params: Seq[Param],
             forwardAlignment : Seq[String],
             forwardMultiSeq: Seq[String]) : Tool = {

            lazy val paramGroups = Map(
              "Input" -> Seq(paramAccess.ALIGNMENT.name, paramAccess.STANDARD_DB.name, paramAccess.HHSUITEDB.name,
                paramAccess.PROTBLASTPROGRAM.name, paramAccess.HHBLITSDB.name, paramAccess.PROTEOMES.name, paramAccess.HMMER_DB.name, paramAccess.REGKEY.name,
                paramAccess.GRAMMAR.name, paramAccess.SAMCC_HELIXONE.name, paramAccess.SAMCC_HELIXTWO.name, paramAccess.SAMCC_HELIXTHREE.name, paramAccess.SAMCC_HELIXFOUR.name)
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
                group ->  paramGroups(group).filter(params.map(_.name).contains(_)).map(paramMap(_))
              }.toSeq :+
                remainParamName -> remainParams.map(paramMap(_))
            )
            Tool(toolNameShort, toolNameLong, toolNameAbbrev, category,optional,paramMap,
               toolitem, paramGroups, forwardAlignment, forwardMultiSeq)
          }


}
