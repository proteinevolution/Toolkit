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
final class ToolFactory @Inject() (paramAccess: ParamAccess, val reactiveMongoApi: ReactiveMongoApi) extends CommonModule{



  def getResults(jobID : String, toolname: String, jobPath: String)(implicit request: Request[AnyContent]): Future[Seq[(String, Html)]] = {
    val resultView =  toolname match {

      case "psiblast" => getResult(jobID).map {
        case Some(jsvalue) => Seq(("Hitlist", views.html.jobs.resultpanels.psiblast.hitlist(jobID, jsvalue)),
          ("E-values", views.html.jobs.resultpanels.evalues(jobID)))
        case None => Seq.empty
      }

      case "clans" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.clans("CLANS", jobID))))

      case "tprpred" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Results", views.html.jobs.resultpanels.tprpred("TPRpred",jobID, jsvalue)))
        case None => Seq.empty
      }

      case "hhblits" => getResult(jobID).map {
        case Some(jsvalue) => Seq(("Hitlist", views.html.jobs.resultpanels.hhblits.hitlist(jobID, jsvalue)),
          ("Full_Alignment", views.html.jobs.resultpanels.alignment("HHblits", "Full-alignment", "full",jsvalue)),
          ("Reduced_Alignment", views.html.jobs.resultpanels.alignment("HHblits", "Reduced-alignment", "reduced" , jsvalue)))
        case None => Seq.empty
      }


      case "marcoil" => Future.successful(Seq(("CC-Prob", views.html.jobs.resultpanels.image(s"/files/$jobID/alignment_ncoils.png")),
        ("ProbState", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.ProbPerState")),
        ("Domains", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.Domains")),
        ("ProbList/PSSM", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/alignment.ProbList"))))

      case "modeller" => Future.successful(Seq(("3D-Structure", views.html.jobs.resultpanels.NGL3DStructure(s"/files/$jobID/$jobID.pdb")),
        ("VERIFY3D", views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.verify3d.png", s"$jobPath$jobID/results/verify3d/$jobID.plotdat")),
        ("SOLVX", views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.solvx.png", s"$jobPath$jobID/results/solvx/$jobID.solvx")),
        ("ANOLEA", views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.anolea.png", s"$jobPath$jobID/results/$jobID.pdb.profile"))))

      case "hmmer" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/outfile")),
        ("Stockholm", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/outfile_multi_sto")),
        ("Domain_Table", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/domtbl"))))

      case "hhpred" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Hitlist", views.html.jobs.resultpanels.hhpred.hitlist(jobID, jsvalue)),
            ("FullAlignment", views.html.jobs.resultpanels.msaviewer(jobID)))
        case None => Seq.empty
      }

      case "hhpred_align" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Hitlist", views.html.jobs.resultpanels.hhpred.hitlist(jobID, jsvalue)),
            ("FullAlignment", views.html.jobs.resultpanels.msaviewer(jobID)))
        case None => Seq.empty
      }

      case "hhpred_manual" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/results.out")),
        ("PIR", views.html.jobs.resultpanels.hhpred.forward(s"$jobPath$jobID/results/tomodel.pir", jobID))))
      case "hhpred_automatic" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/out.hhr"))))


      case "hhrepid" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.modeller(s"/files/$jobID/query_A.png", s"$jobPath$jobID/results/query.hhrepid"))))

      case "ancescon" => Future.successful(Seq(("Tree", views.html.jobs.resultpanels.tree(s"$jobPath$jobID/results/alignment2.clu.tre", "ancescon_div"))))

      case "clustalo" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Alignment", views.html.jobs.resultpanels.alignment("Clustal Omega",jobID, "alignment",jsvalue)),
            ("AlignmentViewer", views.html.jobs.resultpanels.msaviewer(jobID))
          )
        case None => Seq.empty
      }

      case "kalign" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Alignment", views.html.jobs.resultpanels.alignment("Kalign",jobID, "alignment", jsvalue)),
            ("AlignmentViewer", views.html.jobs.resultpanels.msaviewer(jobID))
            )
        case None => Seq.empty
      }

      case "mafft" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Alignment", views.html.jobs.resultpanels.alignment("MAFFT",jobID, "alignment", jsvalue)),
            ("AlignmentViewer", views.html.jobs.resultpanels.msaviewer(jobID))
          )
        case None => Seq.empty
      }

      case "msaprobs" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Alignment", views.html.jobs.resultpanels.alignment("MSAProbs",jobID, "alignment", jsvalue)),
            ("AlignmentViewer", views.html.jobs.resultpanels.msaviewer(jobID))
          )
        case None => Seq.empty
      }

      case "muscle" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Alignment", views.html.jobs.resultpanels.alignment("MUSCLE",jobID, "alignment", jsvalue)),
            ("AlignmentViewer", views.html.jobs.resultpanels.msaviewer(jobID))
          )
        case None => Seq.empty
      }

      case "tcoffee" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Alignment", views.html.jobs.resultpanels.alignment("T-Coffee",jobID, "alignment", jsvalue)),
            ("AlignmentViewer", views.html.jobs.resultpanels.msaviewer(jobID))
          )
        case None => Seq.empty
      }

      case "aln2plot" => Future.successful(Seq(("Plots", views.html.jobs.resultpanels.aln2plot(jobID))))

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
          Seq(("Results", views.html.jobs.resultpanels.unchecked_list("Seq2ID",jobID, jsvalue)))
        case None => Seq.empty
      }

      case "6frametranslation" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileviewWithDownload("6FrameTranslation",s"$jobPath$jobID/results/" + jobID + ".out", jobID))))

      case "backtrans" => Future.successful(Seq(("Results", views.html.jobs.resultpanels.fileview(s"$jobPath$jobID/results/output"))))

      case "hhfilter" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("Alignment", views.html.jobs.resultpanels.alignment("HHfilter",jobID, "alignment", jsvalue)),
            ("AlignmentViewer", views.html.jobs.resultpanels.msaviewer(jobID))
          )
        case None => Seq.empty
      }




      case "patsearch" => getResult(jobID).map {
        case Some(jsvalue) =>
          Seq(("PatternSearch", views.html.jobs.resultpanels.patternSearch("PatternSearch",jobID, "output", jsvalue)))
        case None => Seq.empty
      }
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
      paramAccess.PMIN, paramAccess.MAX_LINES, paramAccess.MAX_SEQS, paramAccess.ALIWIDTH, paramAccess.ALIGNMODE), Seq.empty,Seq.empty),

    // HHpred
    ("hhpred", "HHpred", "hhp", "search", "",
    Seq(paramAccess.PROTEOMES, paramAccess.HHSUITEDB, paramAccess.TWOTEXTALIGNMENT, paramAccess.MSA_GEN_METHOD,
        paramAccess.MSA_GEN_MAX_ITER, paramAccess.SS_SCORING, paramAccess.MACMODE, paramAccess.MACTHRESHOLD,
        paramAccess.MIN_COV, paramAccess.MIN_SEQID_QUERY, paramAccess.EVAL_INC_THRESHOLD,
        paramAccess.MAX_LINES, paramAccess.PMIN, paramAccess.ALIWIDTH, paramAccess.ALIGNMODE), Seq("modeller", "hhpred"),Seq.empty),

    // HHpred - Manual Template Selection
    ("hhpred_manual", "HHpred - ManualTemplate Selection", "hhp", "forward", "",  Seq.empty, Seq.empty,Seq.empty),

    // HHpred - Manual Template Selection
    ("hhpred_automatic", "HHpred - Automatic Template Selection", "hhp", "forward", "",  Seq.empty, Seq.empty,Seq.empty),

    // PSI-BLAST
    ("psiblast", "ProtBLAST/PSI-BLAST", "pbl", "search", "", Seq(paramAccess.SEQORALI, paramAccess.STANDARD_DB,
      paramAccess.MATRIX,
      paramAccess.NUM_ITER, paramAccess.EVALUE, paramAccess.EVAL_INC_THRESHOLD, paramAccess.DESC),
      Seq("modeller", "hhpred"),Seq("modeller")),


    // CLustalOmega
    ("clustalo", "Clustal Omega", "cluo", "alignment", "", Seq(paramAccess.ALIGNMENT,
      paramAccess.OUTPUT_ORDER), Seq("kalign", "tcoffee", "mafft", "msaprobs", "muscle", "hhpred", "hmmer", "hhfilter"),Seq.empty),

    // Kalign
    ("kalign", "Kalign", "kal", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER,
      paramAccess.GAP_OPEN, paramAccess.GAP_EXT_KALN, paramAccess.GAP_TERM, paramAccess.BONUSSCORE),
      Seq("clustalo", "tcoffee", "mafft", "msaprobs", "muscle", "hhpred", "hmmer", "hhfilter"),Seq.empty),

    // T-Coffee
    ("tcoffee", "T-Coffee", "tcf", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER),
      Seq("clustalo", "kalign", "mafft", "msaprobs", "muscle", "hhpred", "hmmer", "hhfilter"),Seq.empty),


    // MAFFT
    ("mafft", "MAFFT", "mft", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER, paramAccess.GAP_OPEN,
      paramAccess.OFFSET), Seq("clustalo", "kalign", "tcoffee", "msaprobs", "muscle", "hhpred", "hmmer", "hhfilter"),Seq.empty),


    // MSA Probs
    ("msaprobs", "MSAProbs", "msap", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER),
      Seq("clustalo", "kalign", "tcoffee", "mafft", "muscle", "hhpred", "hmmer", "hhfilter"),Seq.empty),

    // MUSCLE
    ("muscle", "MUSCLE", "musc", "alignment", "", Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER, paramAccess.MAXROUNDS),
      Seq("clustalo", "kalign", "tcoffee", "mafft", "msaprobs", "hhpred", "hmmer", "hhfilter"),Seq.empty),


    // Hmmer
    ("hmmer", "HMMER", "hmmr", "search", "", Seq(paramAccess.SEQORALI, paramAccess.STANDARD_DB,
      paramAccess.MAX_HHBLITS_ITER, paramAccess.EVAL_CUTOFF), Seq.empty,Seq.empty),


      // Aln2Plot
    ("aln2plot", "Aln2Plot", "a2pl", "seqanal", "", Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // PCOILS
    ("pcoils", "PCOILS", "pco", "seqanal", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.WEIGHTING, paramAccess.MATRIX_PCOILS, paramAccess.RUN_PSIPRED), Seq.empty,Seq.empty),

    // FRrped
    ("frpred", "FRpred", "frp", "seqanal", "",Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // HHrepID
    ("hhrepid", "HHrepID", "hhr", "seqanal", "",Seq(paramAccess.SEQORALI, paramAccess.MSA_GEN_MAX_ITER,
      paramAccess.SCORE_SS, paramAccess.REP_PVAL_THRESHOLD, paramAccess.SELF_ALN_PVAL_THRESHOLD, paramAccess.MERGE_ITERS,
      paramAccess.MAC_CUTOFF, paramAccess.ALN_STRINGENCY, paramAccess.DOMAIN_BOUND_DETECTION), Seq.empty,Seq.empty),

    // MARCOIL
    ("marcoil", "MARCOIL", "mar", "seqanal", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_MARCOIL, paramAccess.TRANSITION_PROBABILITY), Seq.empty,Seq.empty),

    // REPPER
    ("repper", "Repper", "rep", "seqanal", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

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
    ("modeller", "Modeller", "mod", "3ary", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // SamCC
    ("samcc", "SamCC", "sam", "3ary", "",
      Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // RetrieveSeq
    ("retseq", "RetrieveSeq", "ret", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB, paramAccess.UNIQUE_SEQUENCE), Seq.empty,Seq.empty),

    // Seq2ID
    ("seq2id", "Seq2ID", "s2id", "utils", "",
      Seq(paramAccess.FASTAHEADERS), Seq("retseq"),Seq.empty),

    // ANCESCON
    ("ancescon", "ANCESCON", "anc", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.LONG_SEQ_NAME), Seq.empty,Seq.empty),

    // CLANS
      ("clans", "CLANS", "clan", "classification", "",
        Seq(paramAccess.MULTISEQ, paramAccess.MATRIX),
        Seq.empty,Seq.empty),

    // PHYLIP
    ("phylip", "PHYLIP-NEIGHBOR", "phyn", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_PHYLIP), Seq.empty,Seq.empty),

    // MMseqs2
    ("mmseqs2", "MMseqs2", "mseq", "classification", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MIN_SEQID, paramAccess.MIN_ALN_COV), Seq.empty,Seq.empty),

    // Backtranslator
    ("backtrans", "BackTranslator", "bac", "utils", "",
      Seq(paramAccess.SINGLESEQ, paramAccess.INC_AMINO, paramAccess.GENETIC_CODE), Seq.empty,Seq.empty),

    // PatternSearch
    ("patsearch", "PatternSearch", "pats", "search", "",
      Seq(paramAccess.MULTISEQ, paramAccess.STANDARD_DB, paramAccess.GRAMMAR, paramAccess.SEQCOUNT), Seq.empty,Seq.empty),

    // 6FrameTranslation
      ("6frametranslation", "6FrameTranslation", "6frt", "utils", "",
        Seq(paramAccess.SINGLESEQDNA, paramAccess.INC_NUCL, paramAccess.AMINO_NUCL_REL, paramAccess.CODON_TABLE), Seq.empty,Seq.empty),


    // HHfilter
    ("hhfilter", "HHfilter", "hhfi", "utils", "",
      Seq(paramAccess.ALIGNMENT, paramAccess.MAX_SEQID, paramAccess.MIN_SEQID_QUERY, paramAccess.MIN_QUERY_COV,
      paramAccess.NUM_SEQS_EXTRACT), Seq("clustalo", "kalign", "tcoffee", "mafft", "msaprobs", "muscle", "hhpred",
      "hmmer", "hhfilter"),Seq.empty)).map { t =>
    t._1  -> tool(t._1, t._2, t._3, t._4, t._5, t._6, t._7, t._8)
  }.toMap

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
                paramAccess.PROTBLASTPROGRAM.name, paramAccess.HHBLITSDB.name, paramAccess.PROTEOMES.name)
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
