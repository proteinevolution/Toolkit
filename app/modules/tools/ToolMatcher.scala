package modules.tools


import javax.inject.{Inject, Singleton}
import models.tel.TEL
import models.tools.ToolModel
import models.tools.ToolModel._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.RequestHeader
import play.mvc.Http
import play.twirl.api.Html
import reactivemongo.bson.BSONObjectID
import reflect.runtime.universe
import play.api._

/**
  * Created by zin on 20.08.16.
  */



@Singleton
final class ToolMatcher @Inject()( val messagesApi: MessagesApi,
                                   val tel : TEL,
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
      case "alnviz" => views.html.tools.forms.alnviz(Alnviz.inputForm)
      case "tcoffee" => views.html.tools.forms.tcoffee(Tcoffee.inputForm)
      case "hmmer3" => views.html.tools.forms.hmmer3(tel, Hmmer3.inputForm)
      case "psiblast" => views.html.tools.forms.psiblast(tel, Psiblast.inputForm)
      case "mafft" => views.html.tools.forms.mafft(Mafft.inputForm)
      case "csblast" => views.html.tools.forms.csblast(tel, Csblast.inputForm)
      case "hhpred" => views.html.tools.forms.hhpred(tel, HHpred.inputForm)
      case "hhblits" => views.html.tools.forms.hhblits(tel, HHblits.inputForm)
      case "reformatb" => views.html.tools.forms.reformatb(Reformatb.inputForm)
      case "clans" => views.html.tools.forms.clans(tel, Clans.inputForm)
    }
    toolFrame
  }


  def resultPreparedMatcher(tool: String, resultFiles : Map[String, String])(implicit request: RequestHeader) = {
    tool match {
      case "alnviz"   => views.html.tools.forms.alnviz(Alnviz.inputForm.bind(resultFiles))
      case "tcoffee"  => views.html.tools.forms.tcoffee(Tcoffee.inputForm.bind(resultFiles))
      case "hmmer3"   => views.html.tools.forms.hmmer3(tel, Hmmer3.inputForm.bind(resultFiles))
      case "psiblast" => views.html.tools.forms.psiblast(tel, Psiblast.inputForm.bind(resultFiles))
    }
  }


  def resultDoneMatcher(jobID : String, tool: String, mainID: BSONObjectID )(implicit request: RequestHeader) = {

    tool match {
    //  The tool anlviz just returns the BioJS MSA Viewer page
    case "alnviz" =>
      val vis = Map("BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${mainID.stringify}/result"))
      views.html.job.result(vis, jobID, tool)
    // For T-Coffee, we provide a simple alignment visualiation and the BioJS View
    case "tcoffee" =>
      val vis = Map(
        "Simple" -> views.html.visualization.alignment.simple(s"/files/${mainID.stringify}/sequences.clustalw_aln"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${mainID.stringify}/sequences.clustalw_aln"))
      views.html.job.result(vis, jobID, tool)
    case "reformatb" =>
      val vis = Map(
        "Simple" -> views.html.visualization.alignment.simple(s"/files/${mainID.stringify}/sequences.clustalw_aln"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${mainID.stringify}/sequences.clustalw_aln"))
      views.html.job.result(vis, jobID, tool)
    case "psiblast" =>
      val vis = Map(
        "Results" -> views.html.visualization.alignment.blastvis(s"/files/${mainID.stringify}/out.psiblastp"),
        "BioJS" -> views.html.visualization.alignment.msaviewer(s"/files/${mainID.stringify}/sequences.clustalw_aln"),
        "Evalue" -> views.html.visualization.alignment.evalues(s"/files/${mainID.stringify}/evalues.dat"))
      views.html.job.result(vis, jobID, tool)

    // Hmmer just provides a simple file viewer.
    case "hmmer3" => views.html.visualization.general.fileview(
      Array(s"/files/${mainID.stringify}/domtbl",
        s"/files/${mainID.stringify}/outfile",
        s"/files/${mainID.stringify}/outfile_multi_sto",
        s"/files/${mainID.stringify}/tbl"))
    }
  }

  def formMatcher(tool : String) = {
    lazy val toolForm = tool match {
      case "alnviz" => Some(Alnviz.inputForm)
      case "tcoffee" => Some(Tcoffee.inputForm)
      case "hmmer3" => Some(Hmmer3.inputForm)
      case "hhpred" => Some(HHpred.inputForm)
      case "hhblits" => Some(HHblits.inputForm)
      case "psiblast" => Some(Psiblast.inputForm)
      case "mafft" => Some(Mafft.inputForm)
      case "reformatb" => Some(Reformatb.inputForm) // cluster version of reformat
      case "clans" => Some(Clans.inputForm)
      case _ => None
    }
    toolForm
  }
}