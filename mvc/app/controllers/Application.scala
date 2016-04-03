package controllers

import actors.WebSocketActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import play.api.libs.streams.ActorFlow
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.mvc._
import javax.inject.{Inject, Singleton}

import models.sessions.Session
import models.tools.{Alnviz, Hmmer3, Tcoffee}
import actors.MasterConnection


import scala.concurrent.duration._
import scala.concurrent.Future


@Singleton
class Application @Inject()(webJarAssets: WebJarAssets,
                            val messagesApi: MessagesApi,
                            system: ActorSystem,
                            mat: Materializer) extends Controller with I18nSupport {

  val SEP = java.io.File.separator
  val user_id = 12345  // TODO integrate user_id


  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  implicit val timeout = Timeout(5.seconds)

  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"


  def ws = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>

    Logger.info("Application attaches WebSocket")
    Session.requestSessionID(request) match {

      case sid => Future.successful {  Right(ActorFlow.actorRef(WebSocketActor.props(sid, MasterConnection.master))) }
    }
  }


  /**
    * Handles the request of the index page of the toolkit. This will assign a session to the User if
    * not already present.
    * Currently the index controller will assign a session id to the user for identification purpose.
    *
    */
  def index = Action { implicit request =>

    val session_id = Session.requestSessionID(request)

    Ok(views.html.main(webJarAssets, views.html.general.newcontent(),"Home")).withSession {
      Session.closeSessionRequest(request, session_id)
    }
  }

  // Route is handled by Mithril
  def showTool(toolname: String) = Action { implicit request =>

    Redirect(s"/#/tools/$toolname")
  }


  def showJob(job_id : String) = Action { implicit request =>

    Redirect(s"/#/jobs/$job_id")
  }

  /*
    *  Return the Input form of the corresponding tool
    */
  def form(toolname: String) = Action { implicit request =>

    val toolframe = toolname match {
      case "alnviz" => views.html.alnviz.form(Alnviz.inputForm)
      case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm)
      case "hmmer3" => views.html.hmmer3.form(Hmmer3.inputForm)
      case "reformat" => views.html.reformat.form(Hmmer3.inputForm)
    }

    Ok(views.html.general.submit(toolname, toolframe, None)).withSession {

      Session.closeSessionRequest(request, Session.requestSessionID(request)) // Send Session Cookie
    }
  }






  /**
   * Action that offers result files of jobs to the user.
   */
  def file(filename : String, job_id : String) = Action{ implicit request =>

    val session_id = Session.requestSessionID(request)
    val mainID = request.session.get("mid").get

    // main_id exists, allow send File
    Logger.info("Try to assemble file path")
    val filePath = jobPath + "/" + mainID +  "/results/" + filename
    Logger.info("File path was: " + filePath)
    Logger.info("File has been sent")
    Ok.sendFile(new java.io.File(filePath)).withHeaders(CONTENT_TYPE->"text/plain").withSession {
      Session.closeSessionRequest(request, session_id)
    }
  }
}