package controllers


import javax.inject.{Inject, Singleton}

import models.database.{Session, User}
import models.tel.TEL
import models.tools._
import modules.tools.ToolMatcher
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}


/**
  * Created by zin on 03.07.16.
  */


@Singleton
class Forwarding @Inject() (val messagesApi: MessagesApi,
                            val toolMatcher : ToolMatcher,
                            webJarAssets: WebJarAssets) extends Controller with I18nSupport with Session {
  //protected def wja : WebJarAssets

  def forward(toolName: String, output: String) = Action { implicit request =>
    val toolFrame = toolMatcher.matcher(toolName)

    lazy val section : String = toolName match {
      case _ => ""
    }

    lazy val sessionID = requestSessionID
    lazy val user : User = getUser

    Ok(views.html.main(webJarAssets, toolFrame, section, user))
  }

}
