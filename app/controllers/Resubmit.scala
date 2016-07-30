package controllers

import javax.inject.{Inject}


import play.api.i18n.{I18nSupport, MessagesApi}
import play.mvc.Controller

/**
  * Created by zin on 03.07.16.
  */
class Resubmit  @Inject() (webJarAssets: WebJarAssets,
                           val messagesApi: MessagesApi) extends Controller with Forwarding {


  val wja = webJarAssets

}
