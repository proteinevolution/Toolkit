package controllers


import actors.{SubscribeUser, UserManager, WebSocketActor}
import scala.concurrent.Future
import play.api.Play.current
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import javax.inject.Inject


class Application @Inject()(val messagesApi: MessagesApi) extends Controller with I18nSupport {

  val UID = "uid"

  /**
    * Handles the request of the index page of the toolkit. This implies that a new session
    * for the new user will be opened.
    *
    * Currently the index controller will assign a session id to the user for identification purpose
    *
    * @return
    */
  def index = Action { implicit request =>

    Ok(views.html.index())
  }



  // User has connected over the WebSocket
  def ws = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>

    // The user of this session is assigned a user actor
    Future.successful(request.session.get(UID) match {

      case None =>
        Logger.info("WebSocket Connection not allowed, since the user does not have a corresponding session\n")
        Left(Forbidden)

      case Some(uid) =>
        Logger.info("WebSocket has accepted the request with uid " + uid + "\n")
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
}