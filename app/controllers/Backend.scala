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
                              settingsController: Settings,
                              val messagesApi: MessagesApi) extends Controller with I18nSupport {



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

    Ok(views.html.backend.backend(webJarAssets, views.html.backend.settings(settingsController.clusterMode),"Backend", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }
  }


}
