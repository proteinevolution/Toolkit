package controllers

import actors.UserManager.GetUserActor
import actors.WebSocketActor
import akka.actor.{ActorSystem, ActorRef}
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import play.api.libs.streams.ActorFlow
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import javax.inject.{Singleton, Named, Inject}
import models.sessions.Session
import akka.pattern.ask
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global



@Singleton
class Application @Inject()(webJarAssets: WebJarAssets,
                            val messagesApi: MessagesApi,
                            val jobDB : models.database.Jobs,
                            system: ActorSystem,
                            mat: Materializer,
                            @Named("user-manager") userManager : ActorRef) extends Controller with I18nSupport {

  val SEP = java.io.File.separator
  val user_id = 12345  // TODO integrate user_id


  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  implicit val timeout = Timeout(5.seconds)


  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"


  def ws = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>

    Session.requestSessionID(request) match {

      case sid =>

        (userManager ? GetUserActor(sid)).mapTo[ActorRef].map(u =>

          Right(ActorFlow.actorRef(WebSocketActor.props(u)))
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

    val session_id = Session.requestSessionID(request)

    Ok(views.html.main(webJarAssets, views.html.general.newcontent(),"Home")).withSession {
      Session.closeSessionRequest(request, session_id)
    }
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
        val filePath = jobPath + "/" + main_id.toString +  "/results/" + filename
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