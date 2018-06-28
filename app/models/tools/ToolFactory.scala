package models.tools

import javax.inject.{ Inject, Singleton }
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.results._
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.results.ResultViews
import de.proteinevolution.results.results.{ Alignment, HHBlits, HHPred, HHomp }
import de.proteinevolution.services.ToolConfig
import play.api.libs.json.JsArray

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
    toolConfig: ToolConfig
)(resultFiles: ResultFileAccessor, implicit val ec: ExecutionContext) {

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
                                                              toolConfig.values("psiblast"),
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
                toolConfig.values(ToolName.FORMATSEQ.value)
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
                                                             toolConfig.values(ToolName.HHBLITS.value),
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
                  toolConfig.values(ToolName.HHBLITS.value)
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
                                                               toolConfig.values(ToolName.HHBLITS.value))
              case None => views.html.errors.resultnotfound()
            }
          }
        )

      case ToolName.DEEPCOIL.value =>
        ListMap(
          "CC-Prob" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.marcoil(s"/results/files/$jobID/" + jobID + "_deepcoil.png",
                                                   toolConfig.values(ToolName.DEEPCOIL.value))
            )
          },
          "ProbList" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.fileview(s"${constants.jobPath}$jobID/results/" + jobID + "_deepcoil",
                                                    "PCOILS_PROBLIST")
            )
          }
        )

      case ToolName.MARCOIL.value =>
        ListMap(
          "CC-Prob" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.marcoil(s"/results/files/$jobID/alignment_ncoils.png",
                                                   toolConfig.values(ToolName.MARCOIL.value))
            )
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
            Future.successful(views.html.jobs.resultpanels.pcoils(s"/results/files/$jobID/" + jobID))
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
              views.html.jobs.resultpanels.NGL3DStructure(s"/results/files/$jobID/$jobID.pdb",
                                                          jobID + ".pdb",
                                                          jobID,
                                                          "Modeller")
            )
          },
          "SOLVX" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.modeller(s"/results/files/$jobID/$jobID.solvx.png",
                                                    s"${constants.jobPath}$jobID/results/solvx/$jobID.solvx")
            )
          },
          "ANOLEA" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.modeller(s"/results/files/$jobID/$jobID.anolea.png",
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
                                                           toolConfig.values(ToolName.HMMER.value),
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
                                                            toolConfig.values(ToolName.HHPRED.value),
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
                                                       toolConfig.values(ToolName.HHPRED.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          "Query MSA" -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignmentQueryMSA(jobID,
                                                               aln.parse((jsValue \ "reduced").as[JsArray]),
                                                               "reduced",
                                                               toolConfig.values(ToolName.HHPRED.value))
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
                                                           toolConfig.values(ToolName.HHOMP.value),
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
                                                            toolConfig.values(ToolName.HHPRED_ALIGN.value),
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
                                                     toolConfig.values(ToolName.CLUSTALO.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       toolConfig.values(ToolName.CLUSTALO.value))
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
                                                     toolConfig.values(ToolName.KALIGN.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       toolConfig.values(ToolName.KALIGN.value))
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
                                                     toolConfig.values(ToolName.MAFFT.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       toolConfig.values(ToolName.MAFFT.value))
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
                                                     toolConfig.values(ToolName.MSAPROBS.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       toolConfig.values(ToolName.MSAPROBS.value))
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
                                                     toolConfig.values(ToolName.MUSCLE.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       toolConfig.values(ToolName.MUSCLE.value))
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
                                                     toolConfig.values(ToolName.TCOFFEE.value))
              case None => views.html.errors.resultnotfound()
            }
          },
          ResultViews.ALIGNMENT -> { jobID =>
            resultFiles.getResults(jobID).map {
              case Some(jsValue) =>
                views.html.jobs.resultpanels.alignment(jobID,
                                                       aln.parse((jsValue \ "alignment").as[JsArray]),
                                                       "alignment",
                                                       toolConfig.values(ToolName.TCOFFEE.value))
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
                                                                       toolConfig.values(ToolName.MMSEQS2.value))
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
                toolConfig.values(ToolName.RETSEQ.value)
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
              views.html.jobs.resultpanels.NGL3DStructure(s"/results/files/$jobID/$jobID.pdb",
                                                          jobID + ".pdb",
                                                          jobID,
                                                          "samcc_PDB_AXES")
            )
          },
          "Plots" -> { jobID =>
            Future.successful(
              views.html.jobs.resultpanels.samcc(s"/results/files/$jobID/out0.png",
                                                 s"/results/files/$jobID/out1.png",
                                                 s"/results/files/$jobID/out2.png",
                                                 s"/results/files/$jobID/out3.png")
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
                                                       toolConfig.values(ToolName.HHFILTER.value))
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
                views.html.jobs.resultpanels.patternSearch(jobID, jsValue, toolConfig.values(ToolName.PATSEARCH.value))
              case None => views.html.errors.resultnotfound()
            }
          }
        )
    }

  }

}
