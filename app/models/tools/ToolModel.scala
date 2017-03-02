package models.tools

import javax.inject.{Inject, Singleton}
import scala.concurrent._
import ExecutionContext.Implicits.global
import modules.CommonModule
import play.api.mvc.{AnyContent, Request}
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
                params: Map[String, Param],
                toolitem: Toolitem,
                paramGroups: Map[String, Seq[String]],
                forward: Seq[String])


// Class which provides access to all Tools
@Singleton
final class ToolFactory @Inject() (paramAccess: ParamAccess, val reactiveMongoApi: ReactiveMongoApi) extends CommonModule{



  def getResults(jobID : String, toolname: String, jobPath: String)(implicit request: Request[AnyContent]): Future[Seq[(String, Html)]] = {
    val resultView =  toolname match {
      case "psiblast" => Future.successful(Seq(("Hitlist", views.html.jobs.resultpanels.psiblast.hitlist(jobID))))

      case "clans" => Future.successful(Seq.empty)

      case "hhblits" => Future.successful(Seq(("Hitlist", views.html.jobs.resultpanels.hhblits.hitlist(jobID)),
        ("Full_Alignment", views.html.jobs.resultpanels.alignedit("full_alignment", s"/files/$jobID/out.full.fas")),
        ("Reduced_Alignment", views.html.jobs.resultpanels.alignedit("reduced_alignment", s"/files/$jobID/out.reduced.fas"))))

      case "marcoil" => Future.successful(Seq(("CC-Prob", views.html.jobs.resultpanels.image(s"/files/$jobID/alignment_ncoils.png")),
        ("ProbState", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.ProbPerState")),
        ("Domains", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.Domains")),
        ("ProbList/PSSM", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.ProbList"))))

      case "modeller" => Future.successful(Seq(("3D-Structure", views.html.jobs.resultpanels.NGL3DStructure(s"/files/$jobID/$jobID.pdb")),
        ("VERIFY3D", views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.verify3d.png", s"$jobPath$jobID/results/verify3d/$jobID.plotdat")),
        ("SOLVX", views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.solvx.png", s"$jobPath$jobID/results/solvx/$jobID.solvx")),
        ("ANOLEA", views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.anolea.png", s"$jobPath$jobID/results/$jobID.pdb.profile"))))

      case "tcoffee" => Future.successful(Seq(("AlignmentViewer", views.html.jobs.resultpanels.msaviewer_tcoffee(jobID)), ("Conservation", views.html.jobs.resultpanels.tcoffee_colored(jobID)),
        ("Alignment", views.html.jobs.resultpanels.simple(s"/files/$jobID/alignment.clustalw_aln")), ("Text", views.html.jobs.resultpanels.tcoffee_text(jobID))))

      case "hmmer" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/outfile")),
        ("Stockholm", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/outfile_multi_sto")),
        ("Domain_Table", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/domtbl"))))

      case "hhpred" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Hitlist", views.html.jobs.resultpanels.hhpred.hitlist_server(jobID, jsvalue)),
            ("FullAlignment", views.html.jobs.resultpanels.msaviewer_tcoffee(jobID)))
        case None => Seq.empty
      }

      case "hhpred_manual" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/results.out")),
        ("PIR", views.html.jobs.resultpanels.hhpred.forward(s"$jobPath$jobID/results/tomodel.pir", jobID))))
      case "hhpred_automatic" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/out.hhr"))))

      case "ancescon" => Future.successful(Seq(("Tree", views.html.jobs.resultpanels.tree(s"$jobPath$jobID/results/alignment2.clu.tre", "ancescon_div"))))

      case "clustalo" => Future.successful(Seq(("AlignmentViewer", views.html.jobs.resultpanels.msaviewer_tcoffee(jobID)),
        ("Alignment", views.html.jobs.resultpanels.simple(s"/files/$jobID/alignment.clustalw_aln"))))

      case "msaprobs" => Future.successful(Seq(("AlignmentViewer", views.html.jobs.resultpanels.msaviewer_tcoffee(jobID)),
        ("Alignment", views.html.jobs.resultpanels.simple(s"/files/$jobID/alignment.clustalw_aln"))))

      case "muscle" => Future.successful(Seq(("AlignmentViewer", views.html.jobs.resultpanels.msaviewer_tcoffee(jobID)),
        ("Alignment", views.html.jobs.resultpanels.simple(s"/files/$jobID/alignment.clustalw_aln"))))

      case "blammer" => Future.successful(Seq(("AlignmentViewer", views.html.jobs.resultpanels.msaviewer_tcoffee(jobID)),
        ("Alignment", views.html.jobs.resultpanels.simple(s"/files/$jobID/alignment.clustalw_aln"))))

      case "kalign" => Future.successful(Seq(("AlignmentViewer", views.html.jobs.resultpanels.msaviewer_tcoffee(jobID)),
        ("Alignment", views.html.jobs.resultpanels.simple(s"/files/$jobID/alignment.clustalw_aln"))))

      case "mafft" => Future.successful(Seq(("AlignmentViewer", views.html.jobs.resultpanels.msaviewer_tcoffee(jobID)),
        ("Alignment", views.html.jobs.resultpanels.simple(s"/files/$jobID/alignment.clustalw_aln"))))

      case "aln2plot" => Future.successful(Seq(("Hydrophobicity", views.html.jobs.resultpanels.image(s"/files/$jobID/hydrophobicity.png")),
        ("SideChainVolume", views.html.jobs.resultpanels.image(s"/files/$jobID/side_chain_volume.png"))))

      case "phylip" => Future.successful(Seq(("NeighborJoiningTree", views.html.jobs.resultpanels.tree(s"$jobPath$jobID/results/alignment_nj.tree", "nj_div")),
        ("NeighborJoiningResults", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.nj")),
        ("UPGMATree", views.html.jobs.resultpanels.tree(s"$jobPath$jobID/results/alignment_upgma.tree", "upgma_div")),
        ("UPGMAResults", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.upgma"))))

      case "mmseqs2" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/output.rep")),
        ("Summary", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/output.clu"))))

      case "retseq" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/sequences.fa")),
        ("Summary", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/unretrievable"))))

      case "seq2id" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Results", views.html.jobs.resultpanels.checkbox_list(jobID, jsvalue)))
        case None => Seq.empty
      }

      case "6frametranslation" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/output"))))

      case "backtrans" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/output"))))

      case "hhfilter" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/output.fas"))))

      case "patsearch" => Future.successful(Seq(("PatternSearch", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/output.fas"))))

    }

    resultView.map { seq =>
      seq.map { elem =>
        elem._1 -> views.html.jobs.resultpanels.resultpanel(this.values(toolname), elem._2)
      }
    }
  }

  // Contains the tool specifications and generates tool objects accordingly
  lazy val values : Map[String, Tool] = Set(
    // HHblits
    ("hhblits", "HHblits", "hhb", "search", "",
    Seq(paramAccess.SEQORALI,paramAccess.HHBLITSDB, paramAccess.EVAL_INC_THRESHOLD, paramAccess.MAXROUNDS,
      paramAccess.PMIN, paramAccess.MAX_LINES, paramAccess.MAX_SEQS, paramAccess.ALIWIDTH, paramAccess.ALIGNMODE), Seq.empty),

    // HHpred
    ("hhpred", "HHpred", "hhp", "search", "",
    Seq(paramAccess.SEQORALI, paramAccess.HHSUITEDB, paramAccess.MSAGENERATION,
        paramAccess.MSA_GEN_MAX_ITER, paramAccess.MIN_COV, paramAccess.MIN_SEQID_QUERY, paramAccess.EVAL_INC_THRESHOLD,
        paramAccess.MAX_LINES, paramAccess.PMIN, paramAccess.ALIWIDTH, paramAccess.ALIGNMODE, paramAccess.SS_SCORING), Seq("modeller", "hhpred")),

    // HHpred - Manual Template Selection
    ("hhpred_manual", "HHpred - ManualTemplate Selection", "hhp", "forward", "",  Seq.empty, Seq.empty),

    // HHpred - Manual Template Selection
    ("hhpred_automatic", "HHpred - Automatic Template Selection", "hhp", "forward", "",  Seq.empty, Seq.empty),

    // PSI-BLAST
    ("psiblast", "ProtBLAST/PSI-BLAST", "pbl", "search", "", Seq(paramAccess.SEQORALI, paramAccess.STANDARD_DB,
      paramAccess.MATRIX,
      paramAccess.NUM_ITER, paramAccess.EVALUE, paramAccess.EVAL_INC_THRESHOLD, paramAccess.GAP_OPEN,
      paramAccess.GAP_EXT, paramAccess.DESC), Seq.empty),

   // T-Coffee
    ("tcoffee", "T-Coffee", "tcf", "alignment", "", Seq(paramAccess.MULTISEQ), Seq.empty),

    // Blammer
    ("blammer", "Blammer", "blam", "alignment", "", Seq(paramAccess.ALIGNMENT,
      paramAccess.MIN_QUERY_COV, paramAccess.MAX_EVAL, paramAccess.MIN_ANCHOR_WITH,
      paramAccess.MAX_SEQID, paramAccess.MAX_SEQS, paramAccess.MIN_COLSCORE), Seq.empty),

    // CLustalOmega
    ("clustalo", "Clustal Omega", "cluo", "alignment", "", Seq(paramAccess.ALIGNMENT), Seq.empty),

    // MSA Probs
    ("msaprobs", "MSAProbs", "msap", "alignment", "", Seq(paramAccess.MULTISEQ), Seq.empty),

    // MUSCLE
    ("muscle", "MUSCLE", "musc", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.MAXROUNDS), Seq.empty),

  // MAFFT
    ("mafft", "Mafft", "mft", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.GAP_OPEN, paramAccess.OFFSET), Seq.empty),

   // Kalign
      ("kalign", "Kalign", "kal", "alignment", "",
        Seq(paramAccess.MULTISEQ, paramAccess.GAP_OPEN, paramAccess.GAP_EXT, paramAccess.GAP_TERM, paramAccess.BONUSSCORE), Seq.empty),

    // Hmmer
    ("hmmer", "HMMER", "hmmr", "search", "", Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB), Seq.empty),


      // Aln2Plot
    ("aln2plot", "Aln2Plot", "a2pl", "seqanal", "", Seq(paramAccess.ALIGNMENT), Seq.empty),

    // PCOILS
    ("pcoils", "PCOILS", "pco", "seqanal", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.WEIGHTING, paramAccess.MATRIX_PCOILS, paramAccess.RUN_PSIPRED), Seq.empty),

    // FRrped
    ("frpred", "FRpred", "frp", "seqanal", "",Seq(paramAccess.ALIGNMENT), Seq.empty),


    // HHrepID
    ("hhrepid", "HHrepid", "hhr", "seqanal", "",Seq(paramAccess.ALIGNMENT), Seq.empty),


    // MARCOIL
    ("marcoil", "MARCOIL", "mar", "seqanal", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_MARCOIL, paramAccess.TRANSITION_PROBABILITY), Seq.empty),

    // REPPER
    ("repper", "Repper", "rep", "seqanal", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty),

    // TPRpred
    ("tprpred", "TPRpred", "tprp", "seqanal", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty),

    // HHomp
    ("hhomp", "HHomp", "hho", "2ary", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty),

    // Quick 2D
    ("quick2d", "Quick2D", "q2d", "2ary", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty),

    // Ali2D
    ("ali2d", "Ali2D", "a2d", "2ary", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty),

    // Modeller
    ("modeller", "Modeller", "mod", "3ary", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty),

    // RetrieveSeq
    ("retseq", "RetrieveSeq", "ret", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB, paramAccess.UNIQUE_SEQUENCE), Seq.empty),

    // Seq2ID
    ("seq2id", "Seq2ID", "s2id", "utils", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty),

    // ANCESCON
    ("ancescon", "ANCESCON", "anc", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.LONG_SEQ_NAME), Seq.empty),

    // CLANS
      ("clans", "CLANS", "clan", "classification", "",
        Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB, paramAccess.EVALUE, paramAccess.MATRIX,
          paramAccess.NUM_ITER), Seq.empty),

    // PHYLIP
    ("phylip", "PHYLIP-NEIGHBOR", "phyn", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_PHYLIP), Seq.empty),

    // MMseqs2
    ("mmseqs2", "MMseqs2", "mseq", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MIN_SEQID, paramAccess.MIN_ALN_COV), Seq.empty),

    // Backtranslator
    ("backtrans", "Backtranslator", "bac", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.INC_AMINO, paramAccess.GENETIC_CODE), Seq.empty),

    // PatternSearch
    ("patsearch", "PatternSearch", "pats", "search", "",
      Seq(paramAccess.MULTISEQ, paramAccess.STANDARD_DB, paramAccess.GRAMMAR, paramAccess.SEQ_COUNT), Seq.empty),

    // 6FrameTranslation
      ("6frametranslation", "6FrameTranslation", "6frt", "utils", "",
        Seq(paramAccess.ALIGNMENT, paramAccess.INC_NUCL, paramAccess.AMINO_NUCL_REL, paramAccess.CODON_TABLE), Seq.empty),


    // HHfilter
    ("hhfilter", "HHfilter", "hhfi", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MAX_SEQID, paramAccess.MIN_SEQID_QUERY, paramAccess.MIN_QUERY_COV,
        paramAccess.NUM_SEQS_EXTRACT), Seq.empty)).map { t =>
    t._1  -> tool(t._1, t._2, t._3, t._4, t._5, t._6, t._7)
  }.toMap

   // Generates a new Tool object from the Tool specification
    def tool(toolNameShort: String,
             toolNameLong: String,
             toolNameAbbrev: String,
             category: String,
             optional: String,
             params: Seq[Param],
             forward : Seq[String]) : Tool = {

            lazy val paramGroups = Map(
              "Input" -> Seq(paramAccess.ALIGNMENT.name, paramAccess.STANDARD_DB.name, paramAccess.HHSUITEDB.name,
                paramAccess.PROTBLASTPROGRAM.name, paramAccess.HHBLITSDB.name)
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
               toolitem, paramGroups, forward)
          }
}
