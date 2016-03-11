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
    val session_id = Session.requestSessionID(request)
    Ok(views.html.main(views.html.general.homecontent(),"Home")).withSession {
      Session.closeSessionRequest(request, session_id)
    }

    // Without session cookie
    Ok(views.html.main(views.html.general.newcontent(),"Home"))
  }

  /**
   * Action that offers result files of jobs to the user.
   */
  def file(filename : String, job_id : String) = Action{ implicit request =>

    val session_id = Session.requestSessionID(request)
    val main_id_o = jobDB.getMainID(user_id, job_id)

    main_id_o match {
        // main_id exists, allow send File
      case Some(main_id) =>
        Logger.info("Try to assemble file path")
        val filePath = path + "/" + main_id.toString +  "/results/" + filename
        Logger.info("File path was: " + filePath)
        Logger.info("File has been sent")
        Ok.sendFile(new java.io.File(filePath)).withHeaders(CONTENT_TYPE->"text/plain").withSession {
          Session.closeSessionRequest(request, session_id)
        }
        // main_id does not exist. Redirect to NotFound
      case None =>
        NotFound
    }
  }
}