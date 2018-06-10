package controllers

import javax.inject.Inject
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.models.ToolName
import play.api.mvc._
import scala.concurrent.ExecutionContext

class DataController @Inject()(cc: ControllerComponents, resultFiles: ResultFileAccessor)(
    implicit ec: ExecutionContext
) extends AbstractController(cc) {

  /** Check whether the user is allowed to fetch the data for the particular job and retrieves the data with
   * stored given a particular key
   */
  def get(jobID: String): Action[AnyContent] = Action.async {
    resultFiles.getResults(jobID).map {
      case Some(jsValue) => Ok(jsValue)
      case None          => NotFound
    }
  }

  def getHelp(toolname: String) = Action {
    val help = toolname match {
      case ToolName.PSIBLAST.value            => views.html.help.psiblast()
      case ToolName.HHBLITS.value             => views.html.help.hhblits()
      case ToolName.HHPRED.value              => views.html.help.hhpred()
      case ToolName.HMMER.value               => views.html.help.hmmer()
      case ToolName.PATSEARCH.value           => views.html.help.patsearch()
      case ToolName.CLUSTALO.value            => views.html.help.clustalo()
      case ToolName.KALIGN.value              => views.html.help.kalign()
      case ToolName.MAFFT.value               => views.html.help.mafft()
      case ToolName.MSAPROBS.value            => views.html.help.msaprobs()
      case ToolName.MUSCLE.value              => views.html.help.muscle()
      case ToolName.TCOFFEE.value             => views.html.help.tcoffee()
      case ToolName.ALN2PLOT.value            => views.html.help.aln2plot()
      case ToolName.HHREPID.value             => views.html.help.hhrepid()
      case ToolName.MARCOIL.value             => views.html.help.marcoil()
      case ToolName.PCOILS.value              => views.html.help.pcoils()
      case ToolName.REPPER.value              => views.html.help.repper()
      case ToolName.TPRPRED.value             => views.html.help.tprpred()
      case ToolName.ALI2D.value               => views.html.help.ali2d()
      case ToolName.QUICK2D.value             => views.html.help.quick2d()
      case ToolName.MODELLER.value            => views.html.help.modeller()
      case ToolName.SAMCC.value               => views.html.help.samcc()
      case ToolName.ANCESCON.value            => views.html.help.ancescon()
      case ToolName.CLANS.value               => views.html.help.clans()
      case ToolName.MMSEQS2.value             => views.html.help.mmseqs2()
      case ToolName.PHYML.value               => views.html.help.phyml()
      case ToolName.SIXFRAMETRANSLATION.value => views.html.help.sixframe()
      case ToolName.BACKTRANS.value           => views.html.help.backtrans()
      case ToolName.HHFILTER.value            => views.html.help.hhfilter()
      case ToolName.RETSEQ.value              => views.html.help.retseq()
      case ToolName.SEQ2ID.value              => views.html.help.seq2id()
    }
    Ok(help)
  }

  def recentUpdates = Action {
    Ok(views.html.elements.recentupdates())
  }

}
