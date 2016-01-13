package controllers


import actors.{WebSocketActor, WebSocketActor$}
import play.api.routing.JavaScriptReverseRouter

import scala.concurrent.Future
import play.api.Play.current
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import javax.inject.Inject


class Application @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val UID = "uid"
  var counter = 0

  /**
    * Handles the request of the index page of the toolkit. This implies that a new session
    * for the new user will be opened.
    *
    * @return
    */
  def index = Action { implicit request => {

    // Fetch user id from open session
    val uid = request.session.get(UID).getOrElse {
      counter += 1
      counter.toString
    }
    // Continue to index view
    Ok(views.html.index(uid)).withSession {
      Logger.debug("creation uid " + uid)
      request.session + (UID -> uid)
    }
  }
  }

  def ws = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>

    // The user of this session is assigned a user actor
    Future.successful(request.session.get(UID) match {

      case None => Left(Forbidden)
      case Some(uid) =>  Logger.info("WebSocket has accepted the request with uid " + uid)
                         Right(WebSocketActor.props(uid))
    })
  }

  // TODO These Actions must be redefined
  def disclaimer = Action {
    Ok(views.html.disclaimer())
  }




  def contact = Action {
    Ok(views.html.contact())
  }

  def footer = Action {
    Ok(views.html.contact())
  }

  def search = Action {

    Ok(views.html.search())
  }

  def alignment= Action {

    Ok(views.html.search())
  }

  /*
  def index = Action {
    Ok(views.html.index("Bioinformatics Toolkit"))
  }
*/
}