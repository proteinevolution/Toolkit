package de.proteinevolution.help.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.models.ToolName
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.mvc.ControllerComponents

@Singleton
class HelpController @Inject()(config: Configuration, cc: ControllerComponents) extends ToolkitController(cc) {

  def getHelp(toolName: String) = Action {
    val version = getVersion(toolName)
    val help = toolName match {
      case ToolName.PSIBLAST.value            => views.html.help.psiblast(version)
      case ToolName.HHBLITS.value             => views.html.help.hhblits(version)
      case ToolName.HHPRED.value              => views.html.help.hhpred(version)
      case ToolName.HMMER.value               => views.html.help.hmmer(version)
      case ToolName.PATSEARCH.value           => views.html.help.patsearch(version)
      case ToolName.CLUSTALO.value            => views.html.help.clustalo(version)
      case ToolName.KALIGN.value              => views.html.help.kalign(version)
      case ToolName.MAFFT.value               => views.html.help.mafft(version)
      case ToolName.MSAPROBS.value            => views.html.help.msaprobs(version)
      case ToolName.MUSCLE.value              => views.html.help.muscle(version)
      case ToolName.TCOFFEE.value             => views.html.help.tcoffee(version)
      case ToolName.ALN2PLOT.value            => views.html.help.aln2plot(version)
      case ToolName.HHREPID.value             => views.html.help.hhrepid(version)
      case ToolName.MARCOIL.value             => views.html.help.marcoil(version)
      case ToolName.PCOILS.value              => views.html.help.pcoils(version)
      case ToolName.REPPER.value              => views.html.help.repper(version)
      case ToolName.TPRPRED.value             => views.html.help.tprpred(version)
      case ToolName.ALI2D.value               => views.html.help.ali2d(version)
      case ToolName.QUICK2D.value             => views.html.help.quick2d(version)
      case ToolName.MODELLER.value            => views.html.help.modeller(version)
      case ToolName.SAMCC.value               => views.html.help.samcc(version)
      case ToolName.ANCESCON.value            => views.html.help.ancescon(version)
      case ToolName.CLANS.value               => views.html.help.clans(version)
      case ToolName.MMSEQS2.value             => views.html.help.mmseqs2(version)
      case ToolName.PHYML.value               => views.html.help.phyml(version)
      case ToolName.SIXFRAMETRANSLATION.value => views.html.help.sixframe(version)
      case ToolName.BACKTRANS.value           => views.html.help.backtrans(version)
      case ToolName.HHFILTER.value            => views.html.help.hhfilter(version)
      case ToolName.RETSEQ.value              => views.html.help.retseq(version)
      case ToolName.SEQ2ID.value              => views.html.help.seq2id(version)
      case ToolName.DEEPCOIL.value            => views.html.help.deepcoil(version)
    }
    Ok(help)
  }

  private def getVersion(toolName: String): String = {
    config.get[String](s"Tools.$toolName.version")
  }

}
