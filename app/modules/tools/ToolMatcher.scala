package modules.tools


import javax.inject.{Inject, Singleton}

import models.Values
import models.database.Job
import models.tel.TEL
import models.tools.ToolModel._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.RequestHeader
import play.twirl.api.Html

import reflect.runtime.universe

/**
  * Created by zin on 20.08.16.
  *
  * This module mainly exists to show at one glance how badly we need reflection
  *
  */



@Singleton
final class ToolMatcher @Inject()( val messagesApi: MessagesApi,
                                   val tel : TEL,
                                   val values : Values,
                                   val toolMirror: ToolMirror ) extends I18nSupport {



  // This gets the correct view but not the inputForm

  val currentMirror = universe.runtimeMirror(getClass.getClassLoader)
  val packageName = "views.html.tools.forms."
  def loadTemplate(name: String) = {
    val templateName = packageName + name
    val moduleMirror = currentMirror.reflectModule(currentMirror.staticModule(templateName))
    val methodSymbol = moduleMirror.symbol.info.member(universe.TermName("apply")).asMethod
    val instanceMirror = currentMirror.reflect(moduleMirror.instance)
    val methodMirror = instanceMirror.reflectMethod(methodSymbol)
    methodMirror.apply().asInstanceOf[Html]


    // TODO: get this: toolMirror.invokeToolName(name).inputForm and embed this in the template above or alternatively and preferably: ToolModel.withName(name.capitalize).inputForm

  }


  def matcher(tool : String)(implicit request : RequestHeader) : Html = {
    lazy val toolFrame = tool match {

    //  case "tcoffee" => views.html.tools.forms.tcoffee(Tcoffee.inputForm)
      case "hmmer3" => views.html.tools.forms.hmmer3(values, tel, Hmmer3.inputForm)
      //case "psiblast" => views.html.tools.forms.psiblast(values, tel, Psiblast.inputForm)
    //  case "mafft" => views.html.tools.forms.mafft(Mafft.inputForm)
      case "csblast" => views.html.tools.forms.csblast(values, tel, Csblast.inputForm)
      case "hhpred" => views.html.tools.forms.hhpred(values, tel, HHpred.inputForm)
      case "hhblits" => views.html.tools.forms.hhblits(values, tel, HHblits.inputForm)
      case "clans" => views.html.tools.forms.clans(values, tel, Clans.inputForm)
      case "glprobs" => views.html.tools.forms.glprobs(GLProbs.inputForm)
      case "patsearch" => views.html.tools.forms.patSearch(tel, PatSearch.inputForm)
      case "clustalomega" => views.html.tools.forms.clustalomega(ClustalOmega.inputForm)
      case "kalign" => views.html.tools.forms.kalign(Kalign.inputForm)
     //case "muscle" => views.html.tools.forms.muscle(Muscle.inputForm)
    //  case "probcons" => views.html.tools.forms.probcons(ProbCons.inputForm)
    }
    toolFrame
  }


  def resultPreparedMatcher(tool: String, resultFiles : Map[String, String])(implicit request: RequestHeader) = {
    tool match {

     // case "tcoffee"  => views.html.tools.forms.tcoffee(Tcoffee.inputForm.bind(resultFiles))
      case "hmmer3"   => views.html.tools.forms.hmmer3(values, tel, Hmmer3.inputForm.bind(resultFiles))
      //case "psiblast" => views.html.tools.forms.psiblast(values, tel, Psiblast.inputForm.bind(resultFiles))
      // case "mafft" => views.html.tools.forms.mafft(Mafft.inputForm.bind(resultFiles))
      case "glprobs" => views.html.tools.forms.glprobs(GLProbs.inputForm.bind(resultFiles))
      case "clustalomega" => views.html.tools.forms.clustalomega(ClustalOmega.inputForm.bind(resultFiles))
      case "patsearch" => views.html.tools.forms.patSearch(tel, PatSearch.inputForm.bind(resultFiles))
      case "kalign" => views.html.tools.forms.kalign(Kalign.inputForm.bind(resultFiles))
      //case "muscle" => views.html.tools.forms.muscle(Muscle.inputForm.bind(resultFiles))
      //case "probcons" => views.html.tools.forms.probcons(ProbCons.inputForm.bind(resultFiles))
    }
  }


  def resultDoneMatcher(job : Job)(implicit request: RequestHeader) = {

    job.tool match {

    // For T-Coffee, we provide a simple alignment visualiation and the BioJS View
      case "tcoffee" =>
        val vis = Map(
          "Simple" -> views.html.visualization.alignment.simple(s"/files/${job.mainID.stringify}/sequences.clustalw_aln"),
          "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/sequences.clustalw_aln"),
          "Colored" -> views.html.visualization.alignment.tcoffee_extra(s"/files/${job.mainID.stringify}/sequences.score_html"))
      views.html.jobs.result(vis, job)
    case "mafft" =>
      val vis = Map(
        "Simple" -> views.html.visualization.alignment.fasta(s"/files/${job.mainID.stringify}/out"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/out"))
      views.html.jobs.result(vis, job)
    case "psiblast" =>
      val vis = Map(
        "Results" -> views.html.visualization.alignment.blastviz_extra(job.mainID.stringify, s"/files/${job.mainID.stringify}/"),
        "Alignment" -> views.html.visualization.alignment.fasta(s"/files/${job.mainID.stringify}/out.align"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/sequences.clustalw_aln"),
        "Evalue" -> views.html.visualization.alignment.evalues(s"/files/${job.mainID.stringify}/evalues.dat"))
      views.html.jobs.result(vis, job)
    case "patsearch" =>
      val vis = Map(
        "Results" -> views.html.visualization.alignment.fasta(s"/files/${job.mainID.stringify}/patsearch_result"))
      views.html.jobs.result(vis, job)
    case "glprobs" =>
      val vis = Map(
        "Simple" -> views.html.visualization.alignment.fasta(s"/files/${job.mainID.stringify}/glprobs_aln"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/glprobs_aln"))
      views.html.jobs.result(vis, job)

    case "clustalomega" =>
      val vis = Map(
        "Simple" -> views.html.visualization.alignment.simple(s"/files/${job.mainID.stringify}/clustalo_aln"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/clustalo_aln"))
      views.html.jobs.result(vis, job)
    case "kalign" =>
      val vis = Map(
        "Results" -> views.html.visualization.alignment.fasta(s"/files/${job.mainID.stringify}/kalign_aln"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/kalign_aln"))
      views.html.jobs.result(vis, job)
    case "muscle" =>
      val vis = Map(
        "Results" -> views.html.visualization.alignment.fasta(s"/files/${job.mainID.stringify}/muscle_aln"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/muscle_aln"))
      views.html.jobs.result(vis, job)
      case "probcons" =>
        val vis = Map(
          "Results" -> views.html.visualization.alignment.fasta(s"/files/${job.mainID.stringify}/probcons_aln"),
          "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${job.mainID.stringify}/probcons_aln"))
        views.html.jobs.result(vis, job)


      // Hmmer just provides a simple file viewer.
    case "hmmer3" => views.html.visualization.general.fileview(
      Array(s"/files/${job.mainID.stringify}/domtbl",
        s"/files/${job.mainID.stringify}/outfile",
        s"/files/${job.mainID.stringify}/outfile_multi_sto",
        s"/files/${job.mainID.stringify}/tbl"))
    }
  }

  def formMatcher(tool : String) = {
    lazy val toolForm = tool match {

      case "tcoffee" => Some(Tcoffee.inputForm)
      case "hmmer3" => Some(Hmmer3.inputForm)
      case "hhpred" => Some(HHpred.inputForm)
      case "hhblits" => Some(HHblits.inputForm)
      case "psiblast" => Some(Psiblast.inputForm)
      case "mafft" => Some(Mafft.inputForm)
      case "clans" => Some(Clans.inputForm)
      case "patsearch" => Some(PatSearch.inputForm)
      case "glprobs" => Some(GLProbs.inputForm)
      case "clustalomega" => Some(ClustalOmega.inputForm)
      case "kalign" => Some(Kalign.inputForm)
      case "muscle" => Some(Muscle.inputForm)
      case "probcons" => Some(ProbCons.inputForm)
      case _ => None
    }
    toolForm
  }
}