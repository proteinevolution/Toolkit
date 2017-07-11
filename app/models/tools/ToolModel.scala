package models.tools

import javax.inject.{ Inject, Singleton }

import com.typesafe.config.ConfigFactory
import models.Constants
import models.database.results.{ HHBlits, HHPred, Hmmer, PSIBlast }
import modules.db.MongoStore

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.concurrent._
import ExecutionContext.Implicits.global
import play.api.libs.json.JsArray
import play.modules.reactivemongo.ReactiveMongoApi
import play.twirl.api.{ Html, HtmlFormat }

import scala.concurrent.Future

// Returned to the View if a tool is requested with the getTool route
case class Toolitem(toolname: String,
                    toolnameLong: String,
                    toolnameAbbrev: String,
                    category: String,
                    optional: String,
                    params: Seq[(String, Seq[Param])])

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
  def isToolName(toolName: String, caseSensitive: Boolean = false): Boolean = {

    if (toolName.toUpperCase == "REFORMAT" || toolName.toUpperCase == "ALNVIZ")
      true
    else if (caseSensitive) {
      toolNameAbbrev.contains(toolName) || toolNameShort.contains(toolName) || toolNameLong.contains(toolName)
    } else {
      toolNameAbbrev.toLowerCase.contains(toolName.toLowerCase) ||
      toolNameShort.toLowerCase.contains(toolName.toLowerCase) ||
      toolNameLong.toLowerCase.contains(toolName.toLowerCase)
    }
  }
}

// Class which provides access to all Tools
@Singleton
final class ToolFactory @Inject()(
    psi: PSIBlast,
    hmmer: Hmmer,
    hhpred: HHPred,
    hhblits: HHBlits,
    aln: models.database.results.Alignment,
    constants: Constants
)(paramAccess: ParamAccess, mongoStore: MongoStore) {

  // Encompasses all the toolnames
  object Toolnames {

    final val ALNVIZ              = "alnviz"
    final val REFORMAT            = "reformat"
    final val PSIBLAST            = "psiblast"
    final val CLANS               = "clans"
    final val TPRPRED             = "tprpred"
    final val HHBLITS             = "hhblits"
    final val MARCOIL             = "marcoil"
    final val PCOILS              = "pcoils"
    final val MODELLER            = "modeller"
    final val HMMER               = "hmmer"
    final val HHPRED              = "hhpred"
    final val HHPRED_ALIGN        = "hhpred_align"
    final val HHPRED_MANUAL       = "hhpred_manual"
    final val HHREPID             = "hhrepid"
    final val ALI2D               = "ali2d"
    final val CLUSTALO            = "clustalo"
    final val KALIGN              = "kalign"
    final val MAFFT               = "mafft"
    final val MSAPROBS            = "msaprobs"
    final val MUSCLE              = "muscle"
    final val TCOFFEE             = "tcoffee"
    final val ALN2PLOT            = "aln2plot"
    final val ANCESCON            = "ancescon"
    final val PHYML               = "phyml"
    final val MMSEQS2             = "mmseqs2"
    final val RETSEQ              = "retseq"
    final val SEQ2ID              = "seq2id"
    final val SAMCC               = "samcc"
    final val SIXFRAMETRANSLATION = "sixframe"
    final val BACKTRANS           = "backtrans"
    final val HHFILTER            = "hhfilter"
    final val PATSEARCH           = "patsearch"
    final val HHOMP               = "hhomp"
  }

  // Encompasses some shared views of the result pages
  object Resultviews {

    final val HITLIST         = "Hitlist"
    final val RESULTS         = "Results"
    final val ALIGNMENT       = "FASTA Alignment"
    final val CLUSTAL         = "CLUSTAL Alignment"
    final val ALIGNMENTVIEWER = "AlignmentViewer"
    final val TREE            = "Tree"
    final val SUMMARY         = "Summary"
    final val DATA            = "Data"
  }

  // Contains the tool specifications and generates tool objects accordingly
  val values: Map[String, Tool] = Set(
    // HHblits
    ("hhblits",
     Seq(paramAccess.SEQORALI,
         paramAccess.HHBLITSDB,
         paramAccess.HHBLITS_INCL_EVAL,
         paramAccess.MAXROUNDS,
         paramAccess.PMIN,
         paramAccess.DESC),
     Seq("hhblits", "hhpred", "hhrepid"),
     Seq("clans", "mmseqs2")),
      //HHomp
    ("hhomp",
      Seq(paramAccess.SEQORALI,
        paramAccess.HHOMPDB,
        paramAccess.MSA_GEN_METHOD,
        paramAccess.MSA_GEN_MAX_ITER,
        paramAccess.HHPRED_INCL_EVAL,
        paramAccess.MIN_COV,
        paramAccess.MIN_SEQID_QUERY,
        paramAccess.ALIGNMODE,
        paramAccess.PMIN,
        paramAccess.DESC
        ),
      Seq(""),
      Seq("")),
    // HHpred
    ("hhpred",
     Seq(
       paramAccess.PROTEOMES,
       paramAccess.HHSUITEDB,
       paramAccess.TWOTEXTALIGNMENT,
       paramAccess.MSA_GEN_METHOD,
       paramAccess.MSA_GEN_MAX_ITER,
       paramAccess.SS_SCORING,
       paramAccess.MACMODE,
       paramAccess.MACTHRESHOLD,
       paramAccess.MIN_COV,
       paramAccess.MIN_SEQID_QUERY,
       paramAccess.HHPRED_INCL_EVAL,
       paramAccess.DESC,
       paramAccess.PMIN,
       paramAccess.ALIGNMODE
     ),
     Seq("hhblits", "hhpred", "hhrepid"),
     Seq.empty),
    // HHpred - Manual Template Selection
    ("hhpred_manual", Seq.empty, Seq.empty, Seq.empty),
    // PSI-BLAST
    ("psiblast",
     Seq(
       paramAccess.SEQORALI,
       paramAccess.STANDARD_DB,
       paramAccess.MATRIX,
       paramAccess.MAXROUNDS,
       paramAccess.EVALUE,
       paramAccess.HHPRED_INCL_EVAL,
       paramAccess.DESC
     ),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq("clans", "mmseqs2", "seq2id")),
    // CLustalOmega
    ("clustalo",
     Seq(paramAccess.ALIGNMENT, paramAccess.OUTPUT_ORDER),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq.empty),
    // Kalign
    ("kalign",
     Seq(paramAccess.MULTISEQ,
         paramAccess.OUTPUT_ORDER,
         paramAccess.GAP_OPEN,
         paramAccess.GAP_EXT_KALN,
         paramAccess.GAP_TERM,
         paramAccess.BONUSSCORE),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq.empty),
    // T-Coffee
    ("tcoffee",
     Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq.empty),
    // MAFFT
    ("mafft",
     Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER, paramAccess.MAFFT_GAP_OPEN, paramAccess.OFFSET),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq.empty),
    // MSA Probs
    ("msaprobs",
     Seq(paramAccess.MULTISEQ, paramAccess.OUTPUT_ORDER),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq.empty),
    // MUSCLE
    ("muscle",
     Seq(paramAccess.MULTISEQ, paramAccess.MAXROUNDS),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq.empty),
    // Hmmer
    ("hmmer",
     Seq(paramAccess.SEQORALI,
         paramAccess.HMMER_DB,
         paramAccess.MAX_HHBLITS_ITER,
         paramAccess.EVALUE,
         paramAccess.DESC),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq("clans", "mmseqs2")),
    // Aln2Plot
    ("aln2plot", Seq(paramAccess.ALIGNMENT), Seq.empty, Seq.empty),
    // PCOILS
    ("pcoils",
     Seq(paramAccess.ALIGNMENT,
         paramAccess.PCOILS_INPUT_MODE,
         paramAccess.PCOILS_MATRIX,
         paramAccess.PCOILS_WEIGHTING),
     Seq.empty,
     Seq.empty),
    // FRpred; Not for first release
    //("frpred", "FRpred", "frp", "seqanal", "",Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // HHrepID
    ("hhrepid",
     Seq(
       paramAccess.SEQORALI,
       paramAccess.MSA_GEN_MAX_ITER,
       paramAccess.SCORE_SS,
       paramAccess.REP_PVAL_THRESHOLD,
       paramAccess.SELF_ALN_PVAL_THRESHOLD,
       paramAccess.MERGE_ITERS,
       paramAccess.MAC_CUTOFF,
       paramAccess.DOMAIN_BOUND_DETECTION
     ),
     Seq.empty,
     Seq.empty),
    // MARCOIL
    ("marcoil",
     Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_MARCOIL, paramAccess.TRANSITION_PROBABILITY),
     Seq.empty,
     Seq.empty),
    // REPPER Not for first release
    //("repper", "Repper", "rep", "seqanal", "", Seq(paramAccess.ALIGNMENT), Seq.empty,Seq.empty),

    // TPRpred
    ("tprpred", Seq(paramAccess.SINGLESEQ, paramAccess.EVAL_TPR), Seq.empty, Seq.empty),
    // Quick 2D
    //("quick2d", Seq(paramAccess.ALIGNMENT), Seq.empty, Seq.empty),
    // Ali2D
    ("ali2d", Seq(paramAccess.ALIGNMENT, paramAccess.INVOKE_PSIPRED), Seq.empty, Seq.empty),
    // Modeller
    ("modeller", Seq(paramAccess.ALIGNMENT, paramAccess.REGKEY), Seq.empty, Seq.empty),
    // SamCC
    ("samcc",
     Seq(
       paramAccess.ALIGNMENT,
       paramAccess.SAMCC_HELIXONE,
       paramAccess.SAMCC_HELIXTWO,
       paramAccess.SAMCC_HELIXTHREE,
       paramAccess.SAMCC_HELIXFOUR,
       paramAccess.SAMCC_PERIODICITY,
       paramAccess.EFF_CRICK_ANGLE
     ),
     Seq.empty,
     Seq.empty),
    // RetrieveSeq
    ("retseq", Seq(paramAccess.ALIGNMENT, paramAccess.STANDARD_DB), Seq("clans", "mmseqs2"), Seq.empty),
    // Seq2ID
    ("seq2id", Seq(paramAccess.FASTAHEADERS), Seq("retseq"), Seq.empty),
    // ANCESCON
    ("ancescon", Seq(paramAccess.ALIGNMENT), Seq.empty, Seq.empty),
    // CLANS
    ("clans", Seq(paramAccess.MULTISEQ, paramAccess.MATRIX, paramAccess.CLANS_EVAL), Seq.empty, Seq.empty),
    // PhyML
    ("phyml", Seq(paramAccess.ALIGNMENT, paramAccess.MATRIX_PHYML, paramAccess.NO_REPLICATES), Seq.empty, Seq.empty),
    // MMseqs2
    ("mmseqs2",
     Seq(paramAccess.ALIGNMENT, paramAccess.MIN_SEQID, paramAccess.MIN_ALN_COV),
     Seq("clans", "mmseqs2"),
     Seq.empty),
    // Backtranslator
    ("backtrans",
     Seq(paramAccess.SINGLESEQ, paramAccess.INC_AMINO, paramAccess.GENETIC_CODE, paramAccess.CODON_TABLE_ORGANISM),
     Seq.empty,
     Seq.empty),
    // PatternSearch
    ("patsearch",
     Seq(paramAccess.MULTISEQ, paramAccess.PATSEARCH_DB, paramAccess.GRAMMAR, paramAccess.SEQCOUNT),
     Seq("clans", "mmseqs2"),
     Seq.empty),
    // 6FrameTranslation
    ("sixframe",
     Seq(paramAccess.SINGLESEQDNA, paramAccess.INC_NUCL, paramAccess.AMINO_NUCL_REL, paramAccess.CODON_TABLE),
     Seq.empty,
     Seq.empty),
    // HHfilter
    ("hhfilter",
     Seq(paramAccess.ALIGNMENT,
         paramAccess.MAX_SEQID,
         paramAccess.MIN_SEQID_QUERY,
         paramAccess.MIN_QUERY_COV,
         paramAccess.NUM_SEQS_EXTRACT),
     Seq(
       "ali2d",
       "aln2plot",
       "alnviz",
       "ancescon",
       "clans",
       "clustalo",
       "kalign",
       "hhblits",
       "hhfilter",
       "hhpred",
       "hhrepid",
       "hmmer",
       "mafft",
       "mmseqs2",
       "msaprobs",
       "muscle",
       "pcoils",
       "phyml",
       "psiblast",
       "reformat",
       "seq2id",
       "tcoffee"
     ),
     Seq.empty)
  ).map { t =>
    t._1 -> tool(
      t._1,
      ConfigFactory.load().getString(s"Tools.${t._1}.longname"),
      ConfigFactory.load().getString(s"Tools.${t._1}.code"),
      ConfigFactory.load().getString(s"Tools.${t._1}.section").toLowerCase,
      "TODO",
      t._2,
      t._3,
      t._4
    )
  }.toMap

  // Maps toolname and resultpanel name to the function which transfers jobID and jobPath to an appropriate view
  val resultMap: Map[String, ListMap[String, (String, play.api.mvc.RequestHeader) => Future[HtmlFormat.Appendable]]] =
    Map(
      Toolnames.PSIBLAST -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          mongoStore.getResult(jobID).map {

            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.psiblast.hitlist(jobID, psi.parseResult(jsvalue), this.values("psiblast"), s"${constants.jobPath}$jobID/results/blastviz.html")
          }
        },
        "Raw Output" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload("output_psiblastp.html",
                                                              s"${constants.jobPath}$jobID/results/" + "output_psiblastp.html",
                                                              jobID,
                                                              "PSIBLAST_OUTPUT")
          )
        },
        "E-Value Plot" -> { (jobID, requestHeader) =>
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.evalues(psi.parseResult(jsvalue).HSPS.map(_.evalue))
          }
        }
      ),
      Toolnames.CLANS -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.clans("CLANS", jobID))
        }
      ),
      Toolnames.TPRPRED -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.tprpred("TPRpred", jobID, jsvalue)
          }
        }
      ),
      Toolnames.HHBLITS -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.hhblits
                .hitlist(jobID, hhblits.parseResult(jsvalue), this.values(Toolnames.HHBLITS), s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
          }
        },
        "Raw Output (HHR)" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".hhr", s"${constants.jobPath}$jobID/results/" + jobID + ".hhr", jobID, "hhblits_hhr")
          )
        },
        "E-Value Plot" -> { (jobID, requestHeader) =>
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.evalues(hhblits.parseResult(jsvalue).HSPS.map(_.info.evalue))
          }
        },
        "Representative Alignment" -> { (jobID, requestHeader) =>
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "rep100").as[JsArray]),
                                                     "rep100",
                                                     this.values(Toolnames.HHBLITS))
          }
        },
        "Query Template MSA" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "querytemplate").as[JsArray]),
                                                     "querytemplate",
                                                     this.values(Toolnames.HHBLITS))
          }
        }
      ),
      Toolnames.MARCOIL -> ListMap(
        "CC-Prob" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.image(s"/files/$jobID/alignment_ncoils.png"))
        },
        "ProbList" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload("alignment.ProbList",
                                                              s"${constants.jobPath}$jobID/results/alignment.ProbList",
                                                              jobID,
                                                              "marcoil_problist")
          )
        },
        "ProbState" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload("alignment.ProbPerState",
                                                              s"${constants.jobPath}$jobID/results/alignment.ProbPerState",
                                                              jobID,
                                                              "marcoil_probperstate")
          )
        },
        "Predicted Domains" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload("alignment.Domains",
                                                              s"${constants.jobPath}$jobID/results/alignment.Domains",
                                                              jobID,
                                                              "marcoil_domains")
          )
        }
      ),
      Toolnames.PCOILS -> ListMap(
        "CC-Prob" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.image(s"/files/$jobID/" + jobID + "_ncoils.png"))
        },
        "ProbList" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".numerical"))
        }
      ),
      Toolnames.MODELLER -> ListMap(
        "3D-Structure" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.NGL3DStructure(s"/files/$jobID/$jobID.pdb", jobID + ".pdb", jobID, "Modeller")
          )
        },
        "VERIFY3D" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.verify3d.png",
                                                  s"${constants.jobPath}$jobID/results/verify3d/$jobID.plotdat")
          )
        },
        "SOLVX" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.solvx.png",
                                                  s"${constants.jobPath}$jobID/results/solvx/$jobID.solvx")
          )
        },
        "ANOLEA" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.anolea.png",
                                                  s"${constants.jobPath}$jobID/results/$jobID.pdb.profile")
          )
        }
      ),
      Toolnames.HMMER -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.hmmer
                .hitlist(jobID, hmmer.parseResult(jsvalue), this.values(Toolnames.HMMER), s"${constants.jobPath}/$jobID/results/blastviz.html")
          }
        },
        "Raw Output" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".outfilefl",
                                                              s"${constants.jobPath}$jobID/results/" + jobID + ".outfilefl",
                                                              jobID,
                                                              "HMMER_OUTPUT")
          )
        },
        "E-Value Plot" -> { (jobID, requestHeader) =>
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.evalues(hmmer.parseResult(jsvalue).HSPS.map(_.evalue))
          }
        }
      ),
      Toolnames.HHPRED -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.hhpred
                .hitlist(jobID, hhpred.parseResult(jsvalue), this.values(Toolnames.HHPRED), s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
          }
        },
        "Raw Output (HHR)" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".hhr", s"${constants.jobPath}$jobID/results/" + jobID + ".hhr", jobID, "hhpred_hhr")
          )
        },
        "Probability  Plot" -> { (jobID, requestHeader) =>
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.probability(hhpred.parseResult(jsvalue).HSPS.map(_.info.probab))
          }
        },
        "Query Template MSA" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "querytemplate").as[JsArray]),
                                                     "querytemplate",
                                                     this.values(Toolnames.HHPRED))
          }
        },
        "Query MSA" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignmentQueryMSA(jobID,
                                                             aln.parseAlignment((jsvalue \ "reduced").as[JsArray]),
                                                             "reduced",
                                                             this.values(Toolnames.HHPRED))
          }
        }
      ),
      Toolnames.HHPRED_ALIGN -> ListMap(
        Resultviews.HITLIST -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.hhpred
                .hitlist(jobID, hhpred.parseResult(jsvalue), this.values(Toolnames.HHPRED_ALIGN), s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
          }
        },
        "FullAlignment" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln"))
        }
      ),
      Toolnames.HHPRED_MANUAL -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.hhpred.forward(s"${constants.jobPath}$jobID/results/tomodel.pir", jobID))
        },
        Resultviews.SUMMARY -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/results.out"))
        }
      ),
      Toolnames.HHREPID -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.hhrepid(jobID, s"${constants.jobPath}$jobID/results/query.hhrepid",  "querymsa", this.values(Toolnames.HHBLITS)))
        }
      ),
      Toolnames.ALI2D -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results")
          )
        },
        "Colored Results" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results_color")
          )
        },
        "Colored Results With Confidence" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results_colorC")
          )
        }
      ),
      Toolnames.CLUSTALO -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                "alignment",
                this.values(Toolnames.CLUSTALO))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     this.values(Toolnames.CLUSTALO))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.msaviewer(jobID,s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln"))
        }
      ),
      Toolnames.KALIGN -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                "alignment",
                this.values(Toolnames.KALIGN))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     this.values(Toolnames.KALIGN))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln"))
        }
      ),
      Toolnames.MAFFT -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                "alignment",
                this.values(Toolnames.MAFFT))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     this.values(Toolnames.MAFFT))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln"))
        }
      ),
      Toolnames.MSAPROBS -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                "alignment",
                this.values(Toolnames.MSAPROBS))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     this.values(Toolnames.MSAPROBS))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln"))
        }
      ),
      Toolnames.MUSCLE -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                "alignment",
                this.values(Toolnames.MUSCLE))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     this.values(Toolnames.MUSCLE))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln"))
        }
      ),
      Toolnames.TCOFFEE -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                "alignment",
                this.values(Toolnames.TCOFFEE))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     this.values(Toolnames.TCOFFEE))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln"))
        }
      ),
      Toolnames.ALN2PLOT -> ListMap(
        "Plots" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.aln2plot(jobID))
        }
      ),
      Toolnames.ANCESCON -> ListMap(
        Resultviews.TREE -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .tree(jobID + ".clu.tre", s"${constants.jobPath}$jobID/results/" + jobID + ".clu.tre", jobID, "ancescon_output_tree")
          )
        },
        Resultviews.DATA -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".anc_out",
                                                              s"${constants.jobPath}$jobID/results/" + jobID + ".anc_out",
                                                              jobID,
                                                              "ancescon_output_data")
          )
        }
      ),
      Toolnames.PHYML -> ListMap(
        Resultviews.TREE -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.tree(jobID + ".phy_phyml_tree.txt",
                                              s"${constants.jobPath}$jobID/results/" + jobID + ".phy_phyml_tree.txt",
                                              jobID,
                                              "phyml_tree")
          )
        },
        Resultviews.DATA -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".stats",
                                    s"${constants.jobPath}$jobID/results/" + jobID + ".stats",
                                    jobID,
                                    "phyml_data")
          )
        }
      ),
      Toolnames.MMSEQS2 -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownloadForward(jobID + ".fas",
                                           s"${constants.jobPath}$jobID/results/" + jobID + ".fas",
                                           jobID,
                                           "mmseqs_reps",
                                           this.values(Toolnames.MMSEQS2))
          )
        },
        "Clusters" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".clu",
                                    s"${constants.jobPath}$jobID/results/" + jobID + ".clu",
                                    jobID,
                                    "mmseqs_clusters")
          )
        }
      ),
      Toolnames.RETSEQ -> ListMap(
        Resultviews.SUMMARY -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/unretrievable"))
        },
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownloadForward("sequences.fa",
                                                                     s"${constants.jobPath}$jobID/results/sequences.fa",
                                                                     jobID,
                                                                     "retseq",
                                                                     this.values(Toolnames.RETSEQ))
          )
        }
      ),
      Toolnames.SEQ2ID -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.unchecked_list("Seq2ID", jobID, jsvalue, this.values(Toolnames.SEQ2ID))
          }
        }
      ),
      Toolnames.SAMCC -> ListMap(
        "3D-Structure-With-Axes" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .NGL3DStructure(s"/files/$jobID/$jobID.pdb", jobID + ".pdb", jobID, "samcc_PDB_AXES")
          )
        },
        "Plots" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.samcc(s"/files/$jobID/out0.png",
                                               s"/files/$jobID/out1.png",
                                               s"/files/$jobID/out2.png",
                                               s"/files/$jobID/out3.png")
          )
        },
        "NumericalData" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".out", s"${constants.jobPath}$jobID/results/" + jobID + ".out", jobID, "samcc")
          )
        }
      ),
      Toolnames.SIXFRAMETRANSLATION -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".out",
                                                              s"${constants.jobPath}$jobID/results/" + jobID + ".out",
                                                              jobID,
                                                              "sixframetrans_out")
          )
        }
      ),
      Toolnames.BACKTRANS -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".out", s"${constants.jobPath}$jobID/results/" + jobID + ".out", jobID, "backtrans")
          )
        }
      ),
      Toolnames.HHFILTER -> ListMap(
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     this.values(Toolnames.HHFILTER))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln"))
        }
      ),
      Toolnames.PATSEARCH -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          mongoStore.getResult(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels
                .patternSearch("PatternSearch", jobID, jsvalue, this.values(Toolnames.PATSEARCH))
          }
        }
      )
    )

  // Encompasses the names of the resultviews for each tool
  val resultPanels: Map[String, Seq[String]] = this.resultMap.map { trp =>
    trp._1 -> trp._2.keys.toSeq
  }

  // Generates a new Tool object from the Tool specification
  def tool(toolNameShort: String,
           toolNameLong: String,
           toolNameAbbrev: String,
           category: String,
           optional: String,
           params: Seq[Param],
           forwardAlignment: Seq[String],
           forwardMultiSeq: Seq[String]): Tool = {

    lazy val paramGroups = Map(
      "Input" -> Seq(
        paramAccess.ALIGNMENT.name,
        paramAccess.STANDARD_DB.name,
        paramAccess.HHSUITEDB.name,
        paramAccess.PROTBLASTPROGRAM.name,
        paramAccess.HHOMPDB.name,
        paramAccess.HHBLITSDB.name,
        paramAccess.PROTEOMES.name,
        paramAccess.HMMER_DB.name,
        paramAccess.PATSEARCH_DB.name,
        paramAccess.REGKEY.name,
        paramAccess.GRAMMAR.name,
        paramAccess.SAMCC_HELIXONE.name,
        paramAccess.SAMCC_HELIXTWO.name,
        paramAccess.SAMCC_HELIXTHREE.name,
        paramAccess.SAMCC_HELIXFOUR.name
      )
    )
    // Params which are not a part of any group (given by the name)
    lazy val remainParamName: String = "Parameters"
    val remainParams: Seq[String]    = params.map(_.name).diff(paramGroups.values.flatten.toSeq)
    val paramMap                     = params.map(p => p.name -> p).toMap

    val toolitem = Toolitem(
      toolNameShort,
      toolNameLong,
      toolNameAbbrev,
      optional,
      category,
      // Constructs the Parameter specification such that the View can render the input fields
      paramGroups.keysIterator.map { group =>
        group -> paramGroups(group).filter(params.map(_.name).contains(_)).map(paramMap(_))
      }.toSeq :+
      remainParamName -> remainParams.map(paramMap(_))
    )
    Tool(toolNameShort,
         toolNameLong,
         toolNameAbbrev,
         category,
         optional,
         paramMap,
         toolitem,
         paramGroups,
         forwardAlignment,
         forwardMultiSeq)
  }

}
