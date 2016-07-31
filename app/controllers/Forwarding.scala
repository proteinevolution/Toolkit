package controllers


import models.database.User
import models.sessions.Session
import models.tools._

import play.api.i18n.I18nSupport
import play.api.mvc.{Controller, Action}
import play.twirl.api.Html


/**
  * Created by zin on 03.07.16.
  */


private[controllers] trait Forwarding extends Controller with I18nSupport {


  protected def wja : WebJarAssets

  def forward(toolname: String, output: String) = Action { implicit request =>


    lazy val toolframe : Html = toolname match {
      case "alnviz" => views.html.alnviz.form(Alnviz.inputForm)
      case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm)
      case "hmmer3" => views.html.hmmer3.form(Hmmer3.inputForm)
      case "psiblast" => views.html.psiblast.form(Psiblast.inputForm.fill((output, "", "", 1, 10, 11, 1, 200, "")))
      case "mafft" => views.html.mafft.form(Mafft.inputForm)
      case "csblast" => views.html.csblast.form(Csblast.inputForm)
      case "hhpred" => views.html.hhpred.form(HHpred.inputForm)
      case "reformatb" => views.html.reformatb.form(Reformatb.inputForm)
    }

    lazy val section : String = toolname match {
      case _ => ""
    }


    lazy val session_id = Session.requestSessionID(request)
    lazy val user_o : Option[User] = Session.getUser(session_id)

    Ok(views.html.main(wja, toolframe, section, user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }
  }

}
