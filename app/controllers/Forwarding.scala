package controllers


import javax.inject.{Inject, Singleton}

import models.database.{Session, User}
import models.tel.TEL
import models.tools._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}


/**
  * Created by zin on 03.07.16.
  */


@Singleton
class Forwarding @Inject() (val tel : TEL,
                            val messagesApi: MessagesApi,
                            webJarAssets: WebJarAssets) extends Controller with I18nSupport with Session {
  //protected def wja : WebJarAssets

  def forward(toolName: String, output: String) = Action { implicit request =>
    val toolFrame = toolName match {
      case "alnviz"     => views.html.tools.forms.alnviz(Alnviz.inputForm)
      case "tcoffee"    => views.html.tools.forms.tcoffee(Tcoffee.inputForm)
      case "hmmer3"     => views.html.tools.forms.hmmer3(tel, Hmmer3.inputForm)
      case "psiblast"   => views.html.tools.forms.psiblast(tel, Psiblast.inputForm.fill((output, "", "", 1, 10, 11, 1, 200, "")))
      case "mafft"      => views.html.tools.forms.mafft(Mafft.inputForm)
      case "csblast"    => views.html.tools.forms.csblast(tel, Csblast.inputForm)
      case "hhpred"     => views.html.tools.forms.hhpred(tel, HHpred.inputForm)
      case "reformatb"  => views.html.tools.forms.reformatb(Reformatb.inputForm)
      case "clans"      => views.html.tools.forms.clans(tel, Clans.inputForm)
    }

    lazy val section : String = toolName match {
      case _ => ""
    }

    lazy val sessionID = requestSessionID
    lazy val user : User = getUser

    Ok(views.html.main(webJarAssets, toolFrame, section, user)).withSession {
      closeSessionRequest(request, sessionID)
    }
  }

}
