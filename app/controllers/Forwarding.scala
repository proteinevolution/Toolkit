package controllers


import models.database.User
import models.sessions.Session
import models.tools._

import play.api.i18n.I18nSupport
import play.api.mvc.{Controller, Action}


/**
  * Created by zin on 03.07.16.
  */


private[controllers] trait Forwarding extends Controller with I18nSupport {
  protected def wja : WebJarAssets

  def forward(toolName: String, output: String) = Action { implicit request =>
    val toolFrame = toolName match {
      case "alnviz"     => views.html.tools.forms.alnviz(Alnviz.inputForm)
      case "tcoffee"    => views.html.tools.forms.tcoffee(Tcoffee.inputForm)
      case "hmmer3"     => views.html.tools.forms.hmmer3(Hmmer3.inputForm)
      case "psiblast"   => views.html.tools.forms.psiblast(Psiblast.inputForm.fill((output, "", "", 1, 10, 11, 1, 200, "")))
      case "mafft"      => views.html.tools.forms.mafft(Mafft.inputForm)
      case "csblast"    => views.html.tools.forms.csblast(Csblast.inputForm)
      case "hhpred"     => views.html.tools.forms.hhpred(HHpred.inputForm)
      case "reformatb"  => views.html.tools.forms.reformatb(Reformatb.inputForm)
      case "clans"      => views.html.tools.forms.clans(Clans.inputForm)
    }

    lazy val section : String = toolName match {
      case _ => ""
    }

    lazy val sessionID = Session.requestSessionID
    lazy val user : User = Session.getUser

    Ok(views.html.main(wja, toolFrame, section, user)).withSession {
      Session.closeSessionRequest(request, sessionID)
    }
  }

}
