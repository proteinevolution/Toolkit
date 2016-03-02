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
import scala.concurrent.ExecutionContext.Implicits.global
import models.sessions.Session

@Singleton
class Application @Inject()(val messagesApi: MessagesApi,
                            val jobDB : models.database.Jobs,
                            @Named("user-manager") userManager : ActorRef) extends Controller with I18nSupport {

  // TODO this line has to vanish
  var path = s"${Play.application.path}${File.separator}${current.configuration.getString("job_path").get}${File.separator}"


  def ws = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>

    // The user of this session is assigned a user actor
    Future.successful(request.session.get(Session.SID) match {

      case None =>
        Logger.info("$Application$ WebSocket connection not allowed, since no SID has been assigned to the session")
        Left(Forbidden)

      case Some(uid) =>

        Logger.info("$Application$ WebSocket connection requested")
        Right(WebSocketActor.props(uid.toString, userManager))
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

    // TODO Serve reasonble content frame
    Ok(views.html.main(views.html.general.homecontent(),"Home"))

    /** With session cookie
    val session_id = Session.requestSessionID(request)
    Ok(views.html.main(views.html.general.homecontent(),"Home")).withSession {
      Session.closeSessionRequest(request, session_id)
    }
    */
  }

  def file(filename : String, job_id : String) = Action.async { implicit request =>

    // TODO handle the case that there is no userID in session scope or no job with that name
    val session_id = Session.requestSessionID(request)
    val main_id_o = jobDB.userJobMapping.get(session_id.toString, job_id).get

    main_id_o map { main_id =>

      Logger.info("Try to assemble file path")
      val filePath = path + "/" + main_id.toString +  "/results/" + filename
      Logger.info("File has been sent")
      Ok.sendFile(new java.io.File(filePath)).withHeaders(CONTENT_TYPE->"text/plain").withSession {
        Session.closeSessionRequest(request, session_id)
      }
    }
  }
}