package models.tools

import javax.inject.{ Inject, Singleton }

import com.typesafe.config.{ ConfigFactory, ConfigObject }
import de.proteinevolution.models.{ Constants, Tool }
import de.proteinevolution.models.database.results._
import de.proteinevolution.db.{ MongoStore, ResultFileAccessor }
import de.proteinevolution.models.forms.ToolForm
import de.proteinevolution.models.param.{ Param, ParamAccess }
import models.tools.ToolFactory._
import play.api.libs.json.JsArray
import play.twirl.api.HtmlFormat

import scala.collection.JavaConversions._
import scala.collection.immutable.ListMap
import scala.concurrent.{ Future, _ }

// Class which provides access to all Tools
@Singleton
final class ToolFactory @Inject()(
    psi: PSIBlast,
    hmmer: Hmmer,
    hhpred: HHPred,
    hhblits: HHBlits,
    hhomp: HHomp,
    quick2d: Quick2D,
    aln: de.proteinevolution.models.database.results.Alignment,
    constants: Constants
)(paramAccess: ParamAccess,
  mongoStore: MongoStore,
  resultFiles: ResultFileAccessor,
  implicit val ec: ExecutionContext) {

  // reads the tool specifications from tools.conf and generates tool objects accordingly
  lazy val values: Map[String, Tool] = {
    ConfigFactory.load.getConfig("Tools").root.map {
      case (name: String, configObject: ConfigObject) =>
        val config = configObject.toConfig
        config.getString("name") -> toTool(
          config.getString("name"),
          config.getString("longname"),
          config.getString("code"),
          config.getString("section").toLowerCase,
          config.getStringList("parameter").map { param =>
            paramAccess.getParam(param, config.getString("input_placeholder"))
          },
          config.getStringList("forwarding.alignment"),
          config.getStringList("forwarding.multi_seq")
        )
    }
  }.toMap

  def isTool(toolName: String): Boolean = {
    toolName.toUpperCase == "REFORMAT" || toolName.toUpperCase == "ALNVIZ" || values.exists(_._2.isToolName(toolName))
  }

  // Maps toolname and resultpanel name to the function which transfers jobID and jobPath to an appropriate view
  lazy val resultMap
    : Map[String, ListMap[String, (String, play.api.mvc.RequestHeader) => Future[HtmlFormat.Appendable]]] =
    Map(
      Toolnames.PSIBLAST -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.psiblast.hitlist(jobID,
                                                            psi.parseResult(jsvalue),
                                                            values("psiblast"),
                                                            s"${constants.jobPath}$jobID/results/blastviz.html")
          }
        },
        "Raw Output" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload(
              "output_psiblastp.html",
              s"${constants.jobPath}$jobID/results/" + "output_psiblastp.html",
              jobID,
              "PSIBLAST_OUTPUT"
            )
          )
        },
        "E-Value Plot" -> { (jobID, requestHeader) =>
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.evalues(psi.parseResult(jsvalue).HSPS.map(_.evalue))
          }
        }
      ),
      Toolnames.FORMATSEQ -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownloadForward(
              jobID + ".out",
              s"${constants.jobPath}$jobID/results/" + jobID + ".out",
              jobID,
              "FormatSeq",
              values(Toolnames.FORMATSEQ)
            )
          )
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
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.tprpred("TPRpred", jobID, jsvalue)
          }
        }
      ),
      Toolnames.HHBLITS -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.hhblits
                .hitlist(jobID,
                         hhblits.parseResult(jsvalue),
                         values(Toolnames.HHBLITS),
                         s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
          }
        },
        "Raw Output" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".hhr",
                                    s"${constants.jobPath}$jobID/results/" + jobID + ".hhr",
                                    jobID,
                                    "hhblits_hhr")
          )
        },
        "E-Value Plot" -> { (jobID, requestHeader) =>
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.evalues(hhblits.parseResult(jsvalue).HSPS.map(_.info.evalue))
          }
        },
        "Query Template MSA" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignmentQueryMSA(
                jobID,
                aln.parseAlignment((jsvalue \ "querytemplate").as[JsArray]),
                "querytemplate",
                values(Toolnames.HHBLITS)
              )
          }
        },
        "Query Alignment" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignmentQueryMSA(jobID,
                                                             aln.parseAlignment((jsvalue \ "reduced").as[JsArray]),
                                                             "reduced",
                                                             values(Toolnames.HHBLITS))
          }
        }
      ),
      Toolnames.MARCOIL -> ListMap(
        "CC-Prob" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.marcoil(s"/files/$jobID/alignment_ncoils.png"))
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
            views.html.jobs.resultpanels.fileviewWithDownload(
              "alignment.ProbPerState",
              s"${constants.jobPath}$jobID/results/alignment.ProbPerState",
              jobID,
              "marcoil_probperstate"
            )
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
          Future.successful(views.html.jobs.resultpanels.pcoils(s"/files/$jobID/" + jobID))
        },
        "ProbList" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".numerical",
                                                  "PCOILS_PROBLIST")
          )
        }
      ),
      Toolnames.REPPER -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(views.html.jobs.resultpanels.repper(jobID, s"${constants.jobPath}$jobID/results/" + jobID))
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
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.hmmer
                .hitlist(jobID,
                         hmmer.parseResult(jsvalue),
                         values(Toolnames.HMMER),
                         s"${constants.jobPath}/$jobID/results/blastviz.html")
          }
        },
        "E-Value Plot" -> { (jobID, requestHeader) =>
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.evalues(hmmer.parseResult(jsvalue).HSPS.map(_.evalue))
          }
        }
      ),
      Toolnames.HHPRED -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.hhpred
                .hitlist(jobID,
                         hhpred.parseResult(jsvalue),
                         values(Toolnames.HHPRED),
                         s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
          }
        },
        "Raw Output" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".hhr",
                                    s"${constants.jobPath}$jobID/results/" + jobID + ".hhr",
                                    jobID,
                                    "hhpred")
          )
        },
        "Probability  Plot" -> { (jobID, requestHeader) =>
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.probability(hhpred.parseResult(jsvalue).HSPS.map(_.info.probab))
          }
        },
        "Query Template MSA" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "querytemplate").as[JsArray]),
                                                     "querytemplate",
                                                     values(Toolnames.HHPRED))
          }
        },
        "Query MSA" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignmentQueryMSA(jobID,
                                                             aln.parseAlignment((jsvalue \ "reduced").as[JsArray]),
                                                             "reduced",
                                                             values(Toolnames.HHPRED))
          }
        }
      ),
      Toolnames.HHOMP -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.hhomp
                .hitlist(jobID,
                         hhomp.parseResult(jsvalue),
                         values(Toolnames.HHOMP),
                         s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
          }
        },
        "Raw Output" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownload(jobID + ".hhr",
                                    s"${constants.jobPath}$jobID/results/" + jobID + ".hhr",
                                    jobID,
                                    "hhomp_hhr")
          )
        }
      ),
      Toolnames.HHPRED_ALIGN -> ListMap(
        Resultviews.HITLIST -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.hhpred
                .hitlist(jobID,
                         hhpred.parseResult(jsvalue),
                         values(Toolnames.HHPRED_ALIGN),
                         s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
          }
        },
        "FullAlignment" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
          )
        }
      ),
      Toolnames.HHPRED_MANUAL -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.hhpred.forward(s"${constants.jobPath}$jobID/results/tomodel.pir", jobID)
          )
        },
        Resultviews.SUMMARY -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/results.out", "HHPRED_MANUAL")
          )
        }
      ),
      Toolnames.HHREPID -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.hhrepid(jobID,
                                                 s"${constants.jobPath}$jobID/results/query.hhrepid",
                                                 "querymsa",
                                                 values(Toolnames.HHBLITS))
          )
        }
      ),
      Toolnames.ALI2D -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results_color",
                                                  "ALI2D_COLOR")
          )
        },
        "Results With Confidence" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results_colorC",
                                                  "ALI2D_COLOR_CONF")
          )
        },
        "Text output" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results", "ALI2D_TEXT")
          )
        }
      ),
      Toolnames.QUICK2D -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.quick2d(quick2d.parseResult(jsvalue))
          }
        }
      ),
      Toolnames.CLUSTALO -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                                                   aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                   "alignment",
                                                   values(Toolnames.CLUSTALO))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(Toolnames.CLUSTALO))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
          )
        }
      ),
      Toolnames.KALIGN -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                                                   aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                   "alignment",
                                                   values(Toolnames.KALIGN))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(Toolnames.KALIGN))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
          )
        }
      ),
      Toolnames.MAFFT -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                                                   aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                   "alignment",
                                                   values(Toolnames.MAFFT))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(Toolnames.MAFFT))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
          )
        }
      ),
      Toolnames.MSAPROBS -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                                                   aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                   "alignment",
                                                   values(Toolnames.MSAPROBS))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(Toolnames.MSAPROBS))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
          )
        }
      ),
      Toolnames.MUSCLE -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                                                   aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                   "alignment",
                                                   values(Toolnames.MUSCLE))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(Toolnames.MUSCLE))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
          )
        }
      ),
      Toolnames.TCOFFEE -> ListMap(
        Resultviews.CLUSTAL -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.clustal(jobID,
                                                   aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                   "alignment",
                                                   values(Toolnames.TCOFFEE))
          }
        },
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              implicit val r = requestHeader
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(Toolnames.TCOFFEE))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
          )
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
              .tree(jobID + ".clu.tre", s"${constants.jobPath}$jobID/results/" + jobID + ".clu.tre", jobID, "ANCESCON")
          )
        },
        Resultviews.DATA -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownload(
              jobID + ".anc_out",
              s"${constants.jobPath}$jobID/results/" + jobID + ".anc_out",
              jobID,
              "ancescon_output_data"
            )
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
                                              "PhyML")
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
        "Reduced set" -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels
              .fileviewWithDownloadForward(jobID + ".fas",
                                           s"${constants.jobPath}$jobID/results/" + jobID + ".fas",
                                           jobID,
                                           "mmseqs_reps",
                                           values(Toolnames.MMSEQS2))
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
          Future.successful(
            views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/unretrievable", "RETSEQ")
          )
        },
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.fileviewWithDownloadForward(
              "sequences.fa",
              s"${constants.jobPath}$jobID/results/sequences.fa",
              jobID,
              "retseq",
              values(Toolnames.RETSEQ)
            )
          )
        }
      ),
      Toolnames.SEQ2ID -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.unchecked_list("Seq2ID", jobID, jsvalue, values(Toolnames.SEQ2ID))
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
              .fileviewWithDownload(jobID + ".out",
                                    s"${constants.jobPath}$jobID/results/" + jobID + ".out",
                                    jobID,
                                    "samcc")
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
              .fileviewWithDownload(jobID + ".out",
                                    s"${constants.jobPath}$jobID/results/" + jobID + ".out",
                                    jobID,
                                    "backtrans")
          )
        }
      ),
      Toolnames.HHFILTER -> ListMap(
        Resultviews.ALIGNMENT -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels.alignment(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(Toolnames.HHFILTER))
          }
        },
        Resultviews.ALIGNMENTVIEWER -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          Future.successful(
            views.html.jobs.resultpanels.msaviewer(jobID, s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
          )
        }
      ),
      Toolnames.PATSEARCH -> ListMap(
        Resultviews.RESULTS -> { (jobID, requestHeader) =>
          implicit val r = requestHeader
          resultFiles.getResults(jobID).map {
            case Some(jsvalue) =>
              views.html.jobs.resultpanels
                .patternSearch("PatternSearch", jobID, jsvalue, values(Toolnames.PATSEARCH))
          }
        }
      )
    )

  // Encompasses the names of the resultviews for each tool
  val resultPanels: Map[String, Seq[String]] = resultMap.map { trp =>
    trp._1 -> trp._2.keys.toSeq
  }

  // Generates a new Tool object from the Tool specification
  private def toTool(toolNameShort: String,
                     toolNameLong: String,
                     toolNameAbbrev: String,
                     category: String,
                     params: Seq[Param],
                     forwardAlignment: Seq[String],
                     forwardMultiSeq: Seq[String]): Tool = {
    val paramMap = params.map(p => p.name -> p).toMap
    val toolitem = ToolForm(
      toolNameShort,
      toolNameLong,
      toolNameAbbrev,
      category,
      // Constructs the Parameter specification such that the View can render the input fields
      paramAccess.paramGroups.keysIterator.map { group =>
        group -> paramAccess.paramGroups(group).filter(params.map(_.name).contains(_)).map(paramMap(_))
      }.toSeq :+
      "Parameters" -> params.map(_.name).diff(paramAccess.paramGroups.values.flatten.toSeq).map(paramMap(_))
    )
    Tool(
      toolNameShort,
      toolNameLong,
      toolNameAbbrev,
      category,
      paramMap,
      toolitem,
      paramAccess.paramGroups,
      forwardAlignment,
      forwardMultiSeq
    )
  }

}

object ToolFactory {

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
    final val REPPER              = "repper"
    final val MODELLER            = "modeller"
    final val HMMER               = "hmmer"
    final val HHPRED              = "hhpred"
    final val HHPRED_ALIGN        = "hhpred_align"
    final val HHPRED_MANUAL       = "hhpred_manual"
    final val HHREPID             = "hhrepid"
    final val ALI2D               = "ali2d"
    final val QUICK2D             = "quick2d"
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
    final val FORMATSEQ           = "formatseq"
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

}
