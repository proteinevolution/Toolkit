package models.tools

import javax.inject.{ Inject, Singleton }

import com.typesafe.config.{ ConfigFactory, ConfigObject }
import de.proteinevolution.models.{ Constants, Tool, ToolNames }
import de.proteinevolution.models.database.results._
import de.proteinevolution.db.{ MongoStore, ResultFileAccessor }
import de.proteinevolution.models.forms.ToolForm
import de.proteinevolution.models.param.{ Param, ParamAccess }
import de.proteinevolution.models.results.ResultViews
import play.api.libs.json.JsArray
import play.api.mvc.RequestHeader

import scala.collection.JavaConversions._
import scala.collection.immutable.ListMap
import scala.concurrent.{ Future, _ }

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
      case (_, configObject: ConfigObject) =>
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

  def getInnerResultMap(jid: String, τ: String)(
      implicit rh: RequestHeader
  ): ListMap[String, String => Future[play.twirl.api.Html]] = {

    // Constructing this Map is insane, we need a monadic approach, e.g. Reader Monad
    val resultMap: Map[String, ListMap[String, String => Future[play.twirl.api.Html]]] =
      Map(
        ToolNames.PSIBLAST.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.psiblast.hitlist(jobID,
                                                              psi.parseResult(jsvalue),
                                                              values("psiblast"),
                                                              s"${constants.jobPath}$jobID/results/blastviz.html")
            }
          },
          "Raw Output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                "output_psiblastp.html",
                s"${constants.jobPath}$jobID/results/" + "output_psiblastp.html",
                jobID,
                "PSIBLAST_OUTPUT"
              )
            )
          },
          "E-Value Plot" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.evalues(psi.parseResult(jsvalue).HSPS.map(_.evalue))
            }
          }
        ),
        ToolNames.FORMATSEQ.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownloadForward(
                jobID + ".out",
                s"${constants.jobPath}$jobID/results/" + jobID + ".out",
                jobID,
                "FormatSeq",
                values(ToolNames.FORMATSEQ.value)
              )
            )
          }
        ),
        ToolNames.CLANS.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(views.html.jobs.resultpanels.clans("CLANS", jobID))
          }
        ),
        ToolNames.TPRPRED.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.tprpred("TPRpred", jobID, jsvalue)
            }
          }
        ),
        ToolNames.HHBLITS.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.hhblits
                  .hitlist(jobID,
                           hhblits.parseResult(jsvalue),
                           values(ToolNames.HHBLITS.value),
                           s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
            }
          },
          "Raw Output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileviewWithDownload(jobID + ".hhr",
                                      s"${constants.jobPath}$jobID/results/" + jobID + ".hhr",
                                      jobID,
                                      "hhblits_hhr")
            )
          },
          "E-Value Plot" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.evalues(hhblits.parseResult(jsvalue).HSPS.map(_.info.evalue))
            }
          },
          "Query Template MSA" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignmentQueryMSA(
                  jobID,
                  aln.parseAlignment((jsvalue \ "querytemplate").as[JsArray]),
                  "querytemplate",
                  values(ToolNames.HHBLITS.value)
                )
            }
          },
          "Query Alignment" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignmentQueryMSA(jobID,
                                                               aln.parseAlignment((jsvalue \ "reduced").as[JsArray]),
                                                               "reduced",
                                                               values(ToolNames.HHBLITS.value))
            }
          }
        ),
        ToolNames.MARCOIL.value -> ListMap(
          "CC-Prob" -> { jobID =>
            Future.successful(views.html.jobs.resultpanels.marcoil(s"/files/$jobID/alignment_ncoils.png"))
          },
          "ProbList" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                "alignment.ProbList",
                s"${constants.jobPath}$jobID/results/alignment.ProbList",
                jobID,
                "marcoil_problist"
              )
            )
          },
          "ProbState" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                "alignment.ProbPerState",
                s"${constants.jobPath}$jobID/results/alignment.ProbPerState",
                jobID,
                "marcoil_probperstate"
              )
            )
          },
          "Predicted Domains" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload("alignment.Domains",
                                                                s"${constants.jobPath}$jobID/results/alignment.Domains",
                                                                jobID,
                                                                "marcoil_domains")
            )
          }
        ),
        ToolNames.PCOILS.value -> ListMap(
          "CC-Prob" -> { jobID =>
            Future.successful(views.html.jobs.resultpanels.pcoils(s"/files/$jobID/" + jobID))
          },
          "ProbList" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".numerical",
                                                    "PCOILS_PROBLIST")
            )
          }
        ),
        ToolNames.REPPER.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.repper(jobID, s"${constants.jobPath}$jobID/results/" + jobID)
            )
          }
        ),
        ToolNames.MODELLER.value -> ListMap(
          "3D-Structure" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.NGL3DStructure(s"/files/$jobID/$jobID.pdb",
                                                          jobID + ".pdb",
                                                          jobID,
                                                          "Modeller")
            )
          },
          "VERIFY3D" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.verify3d.png",
                                                    s"${constants.jobPath}$jobID/results/verify3d/$jobID.plotdat")
            )
          },
          "SOLVX" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.solvx.png",
                                                    s"${constants.jobPath}$jobID/results/solvx/$jobID.solvx")
            )
          },
          "ANOLEA" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.modeller(s"/files/$jobID/$jobID.anolea.png",
                                                    s"${constants.jobPath}$jobID/results/$jobID.pdb.profile")
            )
          }
        ),
        ToolNames.HMMER.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.hmmer
                  .hitlist(jobID,
                           hmmer.parseResult(jsvalue),
                           values(ToolNames.HMMER.value),
                           s"${constants.jobPath}/$jobID/results/blastviz.html")
            }
          },
          "E-Value Plot" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.evalues(hmmer.parseResult(jsvalue).HSPS.map(_.evalue))
            }
          }
        ),
        ToolNames.HHPRED.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.hhpred
                  .hitlist(jobID,
                           hhpred.parseResult(jsvalue),
                           values(ToolNames.HHPRED.value),
                           s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
            }
          },
          "Raw Output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileviewWithDownload(jobID + ".hhr",
                                      s"${constants.jobPath}$jobID/results/" + jobID + ".hhr",
                                      jobID,
                                      "hhpred")
            )
          },
          "Probability  Plot" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.probability(hhpred.parseResult(jsvalue).HSPS.map(_.info.probab))
            }
          },
          "Query Template MSA" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parseAlignment((jsvalue \ "querytemplate").as[JsArray]),
                                                       "querytemplate",
                                                       values(ToolNames.HHPRED.value))
            }
          },
          "Query MSA" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignmentQueryMSA(jobID,
                                                               aln.parseAlignment((jsvalue \ "reduced").as[JsArray]),
                                                               "reduced",
                                                               values(ToolNames.HHPRED.value))
            }
          }
        ),
        ToolNames.HHOMP.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.hhomp
                  .hitlist(jobID,
                           hhomp.parseResult(jsvalue),
                           values(ToolNames.HHOMP.value),
                           s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
            }
          },
          "Raw Output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileviewWithDownload(jobID + ".hhr",
                                      s"${constants.jobPath}$jobID/results/" + jobID + ".hhr",
                                      jobID,
                                      "hhomp_hhr")
            )
          }
        ),
        ToolNames.HHPRED_ALIGN.value -> ListMap(
          ResultViews.HITLIST -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.hhpred
                  .hitlist(jobID,
                           hhpred.parseResult(jsvalue),
                           values(ToolNames.HHPRED_ALIGN.value),
                           s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
            }
          },
          "FullAlignment" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(jobID,
                                                     s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
            )
          }
        ),
        ToolNames.HHPRED_MANUAL.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.hhpred.forward(s"${constants.jobPath}$jobID/results/tomodel.pir", jobID)
            )
          },
          ResultViews.SUMMARY -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/results.out", "HHPRED_MANUAL")
            )
          }
        ),
        ToolNames.HHREPID.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.hhrepid(jobID,
                                                   s"${constants.jobPath}$jobID/results/query.hhrepid",
                                                   "querymsa",
                                                   values(ToolNames.HHBLITS.value))
            )
          }
        ),
        ToolNames.ALI2D.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results_color",
                                                    "ALI2D_COLOR")
            )
          },
          "Results With Confidence" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results_colorC",
                                                    "ALI2D_COLOR_CONF")
            )
          },
          "Text output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results", "ALI2D_TEXT")
            )
          }
        ),
        ToolNames.QUICK2D.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.quick2d(quick2d.parseResult(jsvalue))
            }
          }
        ),
        ToolNames.CLUSTALO.value -> ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolNames.CLUSTALO.value))
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolNames.CLUSTALO.value))
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(jobID,
                                                     s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
            )
          }
        ),
        ToolNames.KALIGN.value -> ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolNames.KALIGN.value))
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolNames.KALIGN.value))
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(jobID,
                                                     s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
            )
          }
        ),
        ToolNames.MAFFT.value -> ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolNames.MAFFT.value))
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolNames.MAFFT.value))
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(jobID,
                                                     s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
            )
          }
        ),
        ToolNames.MSAPROBS.value -> ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolNames.MSAPROBS.value))
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolNames.MSAPROBS.value))
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(jobID,
                                                     s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
            )
          }
        ),
        ToolNames.MUSCLE.value -> ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolNames.MUSCLE.value))
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolNames.MUSCLE.value))
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(jobID,
                                                     s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
            )
          }
        ),
        ToolNames.TCOFFEE.value -> ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolNames.TCOFFEE.value))
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolNames.TCOFFEE.value))
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(jobID,
                                                     s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
            )
          }
        ),
        ToolNames.ALN2PLOT.value -> ListMap(
          "Plots" -> { jobID =>
            Future.successful(views.html.jobs.resultpanels.aln2plot(jobID))
          }
        ),
        ToolNames.ANCESCON.value -> ListMap(
          ResultViews.TREE -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .tree(jobID + ".clu.tre",
                      s"${constants.jobPath}$jobID/results/" + jobID + ".clu.tre",
                      jobID,
                      "ANCESCON")
            )
          },
          ResultViews.DATA -> { jobID =>
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
        ToolNames.PHYML.value -> ListMap(
          ResultViews.TREE -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.tree(jobID + ".phy_phyml_tree.txt",
                                                s"${constants.jobPath}$jobID/results/" + jobID + ".phy_phyml_tree.txt",
                                                jobID,
                                                "PhyML")
            )
          },
          ResultViews.DATA -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileviewWithDownload(jobID + ".stats",
                                      s"${constants.jobPath}$jobID/results/" + jobID + ".stats",
                                      jobID,
                                      "phyml_data")
            )
          }
        ),
        ToolNames.MMSEQS2.value -> ListMap(
          "Reduced set" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileviewWithDownloadForward(jobID + ".fas",
                                             s"${constants.jobPath}$jobID/results/" + jobID + ".fas",
                                             jobID,
                                             "mmseqs_reps",
                                             values(ToolNames.MMSEQS2.value))
            )
          },
          "Clusters" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileviewWithDownload(jobID + ".clu",
                                      s"${constants.jobPath}$jobID/results/" + jobID + ".clu",
                                      jobID,
                                      "mmseqs_clusters")
            )
          }
        ),
        ToolNames.RETSEQ.value -> ListMap(
          ResultViews.SUMMARY -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/unretrievable", "RETSEQ")
            )
          },
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownloadForward(
                "sequences.fa",
                s"${constants.jobPath}$jobID/results/sequences.fa",
                jobID,
                "retseq",
                values(ToolNames.RETSEQ.value)
              )
            )
          }
        ),
        ToolNames.SEQ2ID.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.unchecked_list("Seq2ID", jobID, jsvalue, values(ToolNames.SEQ2ID.value))
            }
          }
        ),
        ToolNames.SAMCC.value -> ListMap(
          "3D-Structure-With-Axes" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .NGL3DStructure(s"/files/$jobID/$jobID.pdb", jobID + ".pdb", jobID, "samcc_PDB_AXES")
            )
          },
          "Plots" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.samcc(s"/files/$jobID/out0.png",
                                                 s"/files/$jobID/out1.png",
                                                 s"/files/$jobID/out2.png",
                                                 s"/files/$jobID/out3.png")
            )
          },
          "NumericalData" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileviewWithDownload(jobID + ".out",
                                      s"${constants.jobPath}$jobID/results/" + jobID + ".out",
                                      jobID,
                                      "samcc")
            )
          }
        ),
        ToolNames.SIXFRAMETRANSLATION.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".out",
                                                                s"${constants.jobPath}$jobID/results/" + jobID + ".out",
                                                                jobID,
                                                                "sixframetrans_out")
            )
          }
        ),
        ToolNames.BACKTRANS.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels
                .fileviewWithDownload(jobID + ".out",
                                      s"${constants.jobPath}$jobID/results/" + jobID + ".out",
                                      jobID,
                                      "backtrans")
            )
          }
        ),
        ToolNames.HHFILTER.value -> ListMap(
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parseAlignment((jsvalue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolNames.HHFILTER.value))
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(jobID,
                                                     s"${constants.jobPath}/$jobID/results/alignment.clustalw_aln")
            )
          }
        ),
        ToolNames.PATSEARCH.value -> ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsvalue) =>
                views.html.jobs.resultpanels
                  .patternSearch("PatternSearch", jobID, jsvalue, values(ToolNames.PATSEARCH.value))
            }
          }
        )
      )

    for {
      r ← resultMap(τ)
    } yield r

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
    val toolForm = ToolForm(
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
      toolForm,
      paramAccess.paramGroups,
      forwardAlignment,
      forwardMultiSeq
    )
  }

}
