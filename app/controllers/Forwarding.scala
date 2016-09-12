package controllers


import javax.inject.{Inject, Singleton}

import models.tel.TEL
import models.tools._
import modules.tools.ToolMatcher
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.modules.reactivemongo.ReactiveMongoApi
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by zin on 03.07.16.
  */


@Singleton
class Forwarding @Inject() (val messagesApi: MessagesApi,
                            val toolMatcher : ToolMatcher,
@NamedCache("userCache") implicit val userCache  : CacheApi,
                            val reactiveMongoApi : ReactiveMongoApi,
                            webJarAssets: WebJarAssets) extends Controller with I18nSupport with UserSessions {
  //protected def wja : WebJarAssets

  def forward(toolName: String, output: String) = Action.async { implicit request =>
    val toolFrame = toolMatcher.matcher(toolName)

    lazy val section : String = toolName match {
      case _ => ""
    }


    getUser.map { user =>
      Ok(views.html.main(webJarAssets, toolFrame, section, user))
    }
  }
}
