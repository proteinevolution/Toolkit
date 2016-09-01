package modules.tools
import javax.inject.{Inject, Singleton}
import models.tel.TEL
import models.tools.ToolModel._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.RequestHeader
import play.twirl.api.Html


/**
  * Created by zin on 20.08.16.
  */



@Singleton
final class ToolMatcher @Inject()( val messagesApi: MessagesApi,
                             val tel : TEL, val toolMirror: ToolMirror ) extends I18nSupport {



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