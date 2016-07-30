package controllers

import javax.inject.{Inject, Singleton}


import models.database.User
import models.sessions.Session
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, Action}

/**
  * Created by zin on 28.07.16.
  */
@Singleton
final class Backend @Inject()(webJarAssets: WebJarAssets,
                       val messagesApi: MessagesApi) extends Controller with I18nSupport {



  def getStatic(static : String)  = Action { implicit request =>

    static match {

      case "settings" =>
        Ok(views.html.backend.settings("foo")).withSession {
          Session.closeSessionRequest(request, Session.requestSessionID(request))
        }

    }
  }

  def index = Action { implicit request =>

    val session_id = Session.requestSessionID(request)
    val user_o : Option[User] = Session.getUser(session_id)

    Ok(views.html.backend.backend(webJarAssets, views.html.backend.backend_maincontent(),"Backend", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }
  }

  def settings = Action { implicit request =>

    val session_id = Session.requestSessionID(request)
    val user_o : Option[User] = Session.getUser(session_id)

    Ok(views.html.backend.backend(webJarAssets, views.html.backend.settings("foo"),"Backend", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }
  }


}
