package controllers

import actors.UserManager.GetUserActor
import actors.WebSocketActor
import akka.actor.ActorRef
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import play.api.{Configuration, Environment, Logger}
import play.api.Play.current
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import javax.inject.{Singleton, Named, Inject}
import scala.concurrent.Future
import models.sessions.Session
import play.api.Play.materializer
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import java.io.File



@Singleton
class Application @Inject()(val messagesApi: MessagesApi,
                            val jobDB : models.database.Jobs,
                            val environment: Environment,
                            val configuration: Configuration,
                            @Named("user-manager") userManager : ActorRef) extends Controller with I18nSupport {


  val SEP = java.io.File.separator
  val user_id = 12345  // TODO integrate user_id
  implicit val timeout = Timeout(5.seconds)

  var path = s"${environment.rootPath}${File.separator}${ConfigFactory.load().getString("job_path")}${File.separator}"

    //TODO: migrate to akka streams by using flows
  def ws = WebSocket.tryAcceptWithActor[JsValue, JsValue] { implicit request =>
    // The user of this session is assigned a user actor
    request.session.get(Session.SID) match {

      case None =>
        // TODO I guess this should not happen
        Logger.info("$Application$ WebSocket connection not allowed, since no SID has been assigned to the session")
        Future.successful(Left(Forbidden))

      case Some(sid) =>

        (userManager ? GetUserActor(sid)).mapTo[ActorRef].map(u =>

          Right(WebSocketActor.props(u))
        )
    }
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
    *val session_id = Session.requestSessionID(request)
    *Ok(views.html.main(views.html.general.homecontent(),"Home")).withSession {
      *Session.closeSessionRequest(request, session_id)
    *}
    */
  }

  /**
   * Action that offers result files of jobs to the user.
   */
  def file(filename : String, job_id : String) = Action{ implicit request =>

    // TODO handle the case that there is no userID in session scope or no job with that name
    val session_id = Session.requestSessionID(request)
    val main_id = jobDB.getMainID(user_id, job_id)

    val filePath = s"$path$SEP${main_id.toString}${SEP}results$SEP$filename"

    Ok.sendFile(new File(filePath)).withHeaders(CONTENT_TYPE->"text/plain").withSession {
        Session.closeSessionRequest(request, session_id)
    }
  }
}