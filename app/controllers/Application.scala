package controllers


import java.io.File

import actors.WebSocketActor
import akka.actor.ActorRef
import play.api.{Logger, Play}
import play.api.Play.current
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import javax.inject.{Singleton, Named, Inject}

import scala.concurrent.Future

@Singleton
class Application @Inject()(val messagesApi: MessagesApi,
                            @Named("user-manager") userManager : ActorRef) extends Controller with I18nSupport {

  val UID = "uid"
  var path = s"${Play.application.path}${current.configuration.getString("job_path").get}${File.separator}"


  def ws = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>

    // The user of this session is assigned a user actor
    Future.successful(request.session.get(UID) match {

      case None =>
        Logger.info("$Application$ WebSocket connection not allowed, since no UID has been assigned to the session")
        Left(Forbidden)

      case Some(uid) =>
        Logger.info("$Application$ WebSocket connection requested")
        Right(WebSocketActor.props(uid, userManager))
    })
  }



  /**
    * Handles the request of the index page of the toolkit. This implies that a new session
    * for the new user will be opened.
    *
    * Currently the index controller will assign a session id to the user for identification purpose
    *
    * @return
    */
  def index = Action { implicit request =>

    Ok(views.html.general.index())
  }


  def section(sectionname : String) = Action { implicit request =>

    val view = views.html.sections.alignment()

    Ok(views.html.general.main(view))
  }


  /*
  GET         /sections/search                @controllers.Application.section
GET         /sections/alignment             @controllers.Application.alignment
GET         /sections/seqanal               @controllers.Application.search
GET         /sections/secstruct             @controllers.Application.search
GET         /sections/tertstruct            @controllers.Application.search
GET         /sections/classification        @controllers.Application.search
GET         /sections/utils                 @controllers.Application.search
   */


  def file(filename : String, jobid : Long) = Action {

    // TODO check whether the user is allowed to access the file in the jobID

    Ok.sendFile(new java.io.File(path + jobid + "/" + filename)).withHeaders(CONTENT_TYPE->"text/plain")
  }


  def contact = Action {
    Ok(views.html.old.contact())
  }

  def footer = Action {
    Ok(views.html.old.contact())
  }

  def search = Action {

    Ok(views.html.search())
  }

  def alignment= Action {

    Ok(views.html.search())
  }
}