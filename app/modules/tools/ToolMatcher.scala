package modules.tools
import javax.inject.{Inject, Singleton}
import models.tel.TEL
import models.tools._
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.mvc.RequestHeader
import play.twirl.api.Html

/**
  * Created by zin on 20.08.16.
  */



@Singleton
class ToolMatcher @Inject()( val messagesApi: MessagesApi,
                             val tel : TEL ) extends I18nSupport {


  def matcher(tool : String)(implicit request : RequestHeader) : Html = {
    lazy val toolFrame = tool match {
      case "alnviz" => views.html.tools.forms.alnviz(Alnviz.inputForm)
      case "tcoffee" => views.html.tools.forms.tcoffee(Tcoffee.inputForm)
      case "hmmer3" => views.html.tools.forms.hmmer3(tel, Hmmer3.inputForm)
      case "psiblast" => views.html.tools.forms.psiblast(tel, Psiblast.inputForm.fill(("", "", "", 1, 10, 11, 1, 200, "")))
      case "mafft" => views.html.tools.forms.mafft(Mafft.inputForm)
      case "csblast" => views.html.tools.forms.csblast(tel, Csblast.inputForm)
      case "hhpred" => views.html.tools.forms.hhpred(tel, HHpred.inputForm)
      case "hhblits" => views.html.tools.forms.hhblits(tel, HHblits.inputForm)
      case "reformatb" => views.html.tools.forms.reformatb(Reformatb.inputForm)
      case "clans" => views.html.tools.forms.clans(tel, Clans.inputForm)
    }

    toolFrame

  }
}