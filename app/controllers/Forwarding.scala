package controllers

import javax.inject.{Singleton, Inject}

import akka.actor.{ActorSystem}
import akka.stream.Materializer
import models.database.User
import models.sessions.Session
import models.tools._
import org.apache.xerces.impl.io.UTF8Reader
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, Action}
import play.twirl.api.Html
import play.utils.UriEncoding


/**
  * Created by zin on 03.07.16.
  */

@Singleton
class Forwarding @Inject()(webJarAssets: WebJarAssets,
                           val messagesApi: MessagesApi,
                           system: ActorSystem,
                           mat: Materializer,
                           configuration: Configuration) extends Controller with I18nSupport {



  def forward(toolname: String, output: String) = Action { implicit request =>

    //Redirect(s"/#/tools/$toolname")
    //return ok(form.render(userForm,));

    val toolframe : Html = toolname match {
      case "alnviz" => views.html.alnviz.form(Alnviz.inputForm)
      case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm)
      case "hmmer3" => views.html.hmmer3.form(Hmmer3.inputForm)
      case "psiblast" => views.html.psiblast.form(Psiblast.inputForm.fill((output, "", "", 1, 10, 11, 1, 200, "")))
      case "mafft" => views.html.mafft.form(Mafft.inputForm)
      case "csblast" => views.html.csblast.form(Csblast.inputForm)
      case "hhpred" => views.html.hhpred.form(HHpred.inputForm)
      case "reformatb" => views.html.reformatb.form(Reformatb.inputForm)
    }


    val session_id = Session.requestSessionID(request)
    val user_o : Option[User] = Session.getUser(session_id)

    Ok(views.html.main(webJarAssets, views.html.general.submit(toolname, toolframe, None),"Home", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }


  }


}
