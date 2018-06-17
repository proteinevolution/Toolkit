package models.tools

import javax.inject.{ Inject, Singleton }
import com.typesafe.config.{ Config, ConfigObject }
import de.proteinevolution.models.{ ConstantsV2, Tool, ToolName }
import de.proteinevolution.results.results._
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.forms.ToolForm
import de.proteinevolution.models.param.{ Param, ParamAccess }
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.{ Alignment, HHBlits, HHPred, HHomp }
import play.api.Configuration
import play.api.libs.json.JsArray

import scala.collection.JavaConverters._
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
    aln: Alignment,
    constants: ConstantsV2,
    config: Configuration
)(paramAccess: ParamAccess, resultFiles: ResultFileAccessor, implicit val ec: ExecutionContext) {

  // reads the tool specifications from tools.conf and generates tool objects accordingly
  lazy val values: Map[String, Tool] = {
    config.get[Config]("Tools").root.asScala.map {
      case (_, configObject: ConfigObject) =>
        val config = configObject.toConfig
        config.getString("name") -> toTool(
          config.getString("name"),
          config.getString("longname"),
          config.getString("code"),
          config.getString("section").toLowerCase,
          config.getStringList("parameter").asScala.map { param =>
            paramAccess.getParam(param, config.getString("input_placeholder"))
          },
          config.getStringList("forwarding.alignment").asScala,
          config.getStringList("forwarding.multi_seq").asScala,
          config.getString("title")
        )
      case (_, _) => throw new IllegalStateException("tool does not exist")
    }
  }.toMap

  def isTool(toolName: String): Boolean = {
    toolName.toUpperCase == "REFORMAT" || toolName.toUpperCase == "ALNVIZ" || values.exists {
      case (_, tool) =>
        tool.isToolName(toolName)
      case _ => false
    }
  }

  // Maps toolname and resultpanel name to the function which transfers jobID and jobPath to an appropriate view

  def getResultMap(τ: String): ListMap[String, String => Future[play.twirl.api.Html]] = {

    τ match {
      case ToolName.PSIBLAST.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.psiblast.hitlist(jobID,
                                                              psi.parseResult(jsValue),
                                                              values("psiblast"),
                                                              s"${constants.jobPath}$jobID/results/blastviz.html")
              case None => views.html.errors.resultnotfound()
            }
          },
          "Raw Output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                "output_psiblastp.html",
                jobID,
                "PSIBLAST_OUTPUT"
              )
            )
          },
          "E-Value Plot" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.evalues(psi.parseResult(jsValue).HSPS.map(_.evalue))
              case None => views.html.errors.resultnotfound()
            }
          }
        )
      case ToolName.FORMATSEQ.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownloadForward(
                jobID + ".out",
                jobID,
                "FormatSeq",
                values(ToolName.FORMATSEQ.value)
              )
            )
          }
        )
      case ToolName.CLANS.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(views.html.jobs.resultpanels.clans(jobID))
          }
        )
      case ToolName.TPRPRED.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.tprpred(jobID, jsValue)
              case None => views.html.errors.resultnotfound()
            }
          }
        )
      case ToolName.HHBLITS.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.hhblits.hitlist(jobID,
                                                             hhblits.parseResult(jsValue),
                                                             values(ToolName.HHBLITS.value),
                                                             s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
              case None => views.html.errors.resultnotfound()
            }
          },
          "Raw Output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".hhr", jobID, "hhblits_hhr")
            )
          },
          "E-Value Plot" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.evalues(hhblits.parseResult(jsValue).HSPS.map(_.info.evalue))
              case None => views.html.errors.resultnotfound()
            }
          },
          "Query Template MSA" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignmentQueryMSA(
                  jobID,
                  aln.parse((jsValue \ "querytemplate").as[JsArray]),
                  "querytemplate",
                  values(ToolName.HHBLITS.value)
                )
              case None => views.html.errors.resultnotfound()
            }
          },
          "Query Alignment" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignmentQueryMSA(jobID,
                                                               aln.parse((jsValue \ "reduced").as[JsArray]),
                                                               "reduced",
                                                               values(ToolName.HHBLITS.value))
              case None => views.html.errors.resultnotfound()
            }
          }
        )
      case ToolName.MARCOIL.value =>
        ListMap(
          "CC-Prob" -> { jobID =>
            Future.successful(views.html.jobs.resultpanels.marcoil(s"/files/$jobID/alignment_ncoils.png"))
          },
          "ProbList" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                "alignment.ProbList",
                jobID,
                "marcoil_problist"
              )
            )
          },
          "ProbState" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                "alignment.ProbPerState",
                jobID,
                "marcoil_probperstate"
              )
            )
          },
          "Predicted Domains" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                "alignment.Domains",
                jobID,
                "marcoil_domains"
              )
            )
          }
        )
      case ToolName.PCOILS.value =>
        ListMap(
          "CC-Prob" -> { jobID =>
            Future.successful(views.html.jobs.resultpanels.pcoils(s"/files/$jobID/" + jobID))
          },
          "ProbList" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".numerical",
                                                    "PCOILS_PROBLIST")
            )
          }
        )
      case ToolName.REPPER.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.repper(jobID, s"${constants.jobPath}$jobID/results/" + jobID)
            )
          }
        )
      case ToolName.MODELLER.value =>
        ListMap(
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
        )
      case ToolName.HMMER.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.hmmer.hitlist(jobID,
                                                           hmmer.parseResult(jsValue),
                                                           values(ToolName.HMMER.value),
                                                           s"${constants.jobPath}/$jobID/results/blastviz.html")
              case None => views.html.errors.resultnotfound()
            }
          },
          "E-Value Plot" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.evalues(hmmer.parseResult(jsValue).HSPS.map(_.evalue))
              case None => views.html.errors.resultnotfound()
            }
          }
        )
      case ToolName.HHPRED.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.hhpred.hitlist(jobID,
                                                            hhpred.parseResult(jsValue),
                                                            values(ToolName.HHPRED.value),
                                                            s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
              case None => views.html.errors.resultnotfound()
            }
          },
          "Raw Output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".hhr", jobID, "hhpred")
            )
          },
          "Probability  Plot" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.probability(hhpred.parseResult(jsValue).HSPS.map(_.info.probab))
              case None => views.html.errors.resultnotfound()
            }
          },
          "Query Template MSA" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "querytemplate").as[JsArray]),
                                                       "querytemplate",
                                                       values(ToolName.HHPRED.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          "Query MSA" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignmentQueryMSA(jobID,
                                                               aln.parse((jsValue \ "reduced").as[JsArray]),
                                                               "reduced",
                                                               values(ToolName.HHPRED.value))
              case None => views.html.errors.resultnotfound()
            }
          }
        )
      case ToolName.HHOMP.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.hhomp.hitlist(jobID,
                                                           hhomp.parseResult(jsValue),
                                                           values(ToolName.HHOMP.value),
                                                           s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
              case None => views.html.errors.resultnotfound()
            }
          },
          "Raw Output" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".hhr", jobID, "hhomp")
            )
          }
        )
      case ToolName.HHPRED_ALIGN.value =>
        ListMap(
          ResultViews.HITLIST -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.hhpred.hitlist(jobID,
                                                            hhpred.parseResult(jsValue),
                                                            values(ToolName.HHPRED_ALIGN.value),
                                                            s"${constants.jobPath}/$jobID/results/$jobID.html_NOIMG")
              case None => views.html.errors.resultnotfound()
            }
          },
          "FullAlignment" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(s"${constants.jobPath}/$jobID/results/alignment.fas")
            )
          }
        )
      case ToolName.HHPRED_MANUAL.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.hhpred.forward(s"${constants.jobPath}$jobID/results/tomodel.pir")
            )
          },
          ResultViews.SUMMARY -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/results.out", "HHPRED_MANUAL")
            )
          }
        )
      case ToolName.HHREPID.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.hhrepid(jobID, s"${constants.jobPath}$jobID/results/query.hhrepid")
            )
          }
        )
      case ToolName.ALI2D.value =>
        ListMap(
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
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + ".results",
                                                    "ALI2D_TEXT")
            )
          }
        )
      case ToolName.QUICK2D.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.quick2d(quick2d.parseResult(jsValue))
              case None => views.html.errors.resultnotfound()
            }
          }
        )
      case ToolName.CLUSTALO.value =>
        ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parse((jsValue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolName.CLUSTALO.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolName.CLUSTALO.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(s"${constants.jobPath}/$jobID/results/alignment.fas")
            )
          }
        )
      case ToolName.KALIGN.value =>
        ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parse((jsValue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolName.KALIGN.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolName.KALIGN.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(s"${constants.jobPath}/$jobID/results/alignment.fas")
            )
          }
        )
      case ToolName.MAFFT.value =>
        ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parse((jsValue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolName.MAFFT.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolName.MAFFT.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(s"${constants.jobPath}/$jobID/results/alignment.fas")
            )
          }
        )
      case ToolName.MSAPROBS.value =>
        ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parse((jsValue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolName.MSAPROBS.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolName.MSAPROBS.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(s"${constants.jobPath}/$jobID/results/alignment.fas")
            )
          }
        )
      case ToolName.MUSCLE.value =>
        ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parse((jsValue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolName.MUSCLE.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolName.MUSCLE.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(s"${constants.jobPath}/$jobID/results/alignment.fas")
            )
          }
        )
      case ToolName.TCOFFEE.value =>
        ListMap(
          ResultViews.CLUSTAL -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.clustal(jobID,
                                                     aln.parse((jsValue \ "alignment").as[JsArray]),
                                                     "alignment",
                                                     values(ToolName.TCOFFEE.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolName.TCOFFEE.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(s"${constants.jobPath}/$jobID/results/alignment.fas")
            )
          }
        )
      case ToolName.ALN2PLOT.value =>
        ListMap(
          "Plots" -> { jobID =>
            Future.successful(views.html.jobs.resultpanels.aln2plot(jobID))
          }
        )
      case ToolName.ANCESCON.value =>
        ListMap(
          ResultViews.TREE -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.tree(jobID + ".clu.tre",
                                                s"${constants.jobPath}$jobID/results/" + jobID + ".clu.tre",
                                                jobID,
                                                "ANCESCON")
            )
          },
          ResultViews.DATA -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                jobID + ".anc_out",
                jobID,
                "ancescon_output_data"
              )
            )
          }
        )
      case ToolName.PHYML.value =>
        ListMap(
          ResultViews.TREE -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.tree(
                jobID + ".phy_phyml_tree.txt",
                s"${constants.jobPath}$jobID/results/" + jobID + ".phy_phyml_tree.txt",
                jobID,
                "PhyML"
              )
            )
          },
          ResultViews.DATA -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".stats", jobID, "phyml_data")
            )
          }
        )
      case ToolName.MMSEQS2.value =>
        ListMap(
          "Reduced set" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownloadForward(jobID + ".fas",
                                                                       jobID,
                                                                       "mmseqs_reps",
                                                                       values(ToolName.MMSEQS2.value))
            )
          },
          "Clusters" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".clu", jobID, "mmseqs_clusters")
            )
          }
        )
      case ToolName.RETSEQ.value =>
        ListMap(
          ResultViews.SUMMARY -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/unretrievable", "RETSEQ")
            )
          },
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownloadForward(
                "sequences.fa",
                jobID,
                "retseq",
                values(ToolName.RETSEQ.value)
              )
            )
          }
        )
      case ToolName.SEQ2ID.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.unchecked_list("seq2id", jobID, jsValue)
              case None => views.html.errors.resultnotfound()
            }
          }
        )
      case ToolName.SAMCC.value =>
        ListMap(
          "3D-Structure-With-Axes" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.NGL3DStructure(s"/files/$jobID/$jobID.pdb",
                                                          jobID + ".pdb",
                                                          jobID,
                                                          "samcc_PDB_AXES")
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
              views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".out", jobID, "samcc")
            )
          }
        )
      case ToolName.SIXFRAMETRANSLATION.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(
                jobID + ".out",
                jobID,
                "sixframetrans"
              )
            )
          }
        )
      case ToolName.BACKTRANS.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileviewWithDownload(jobID + ".out", jobID, "backtrans")
            )
          }
        )
      case ToolName.HHFILTER.value =>
        ListMap(
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       values(ToolName.HHFILTER.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENTVIEWER -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.msaviewer(s"${constants.jobPath}/$jobID/results/alignment.fas")
            )
          }
        )
      case ToolName.PATSEARCH.value =>
        ListMap(
          ResultViews.RESULTS -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.patternSearch(jobID, jsValue, values(ToolName.PATSEARCH.value))
              case None => views.html.errors.resultnotfound()
            }
          }
        )
    }

  }

  // Generates a new Tool object from the Tool specification
  private def toTool(toolNameShort: String,
                     toolNameLong: String,
                     toolNameAbbrev: String,
                     category: String,
                     params: Seq[Param],
                     forwardAlignment: Seq[String],
                     forwardMultiSeq: Seq[String],
                     title: String): Tool = {
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
      forwardMultiSeq,
      title
    )
  }

}
