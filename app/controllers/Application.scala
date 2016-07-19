package controllers

import actors.WebSocketActor
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import models.database.User
import play.api.libs.streams.ActorFlow
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import javax.inject.{Inject, Named, Singleton}

import models.sessions.Session
import models.tools._
import play.api.mvc.Results._

import scala.concurrent.duration._
import scala.concurrent.Future
import play.api.mvc._
import play.api.Configuration
import play.twirl.api.Html


@Singleton
class Application @Inject()(webJarAssets: WebJarAssets,
                            val messagesApi: MessagesApi,
                            system: ActorSystem,
                            mat: Materializer,
                            @Named("jobManager") jobManager : ActorRef,    // Connect to JobManager
                            configuration: Configuration) extends Controller with I18nSupport {


  val SEP = java.io.File.separator
  val user_id = 12345  // TODO integrate user_id


  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  implicit val timeout = Timeout(5.seconds)

  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"




  def ws = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>

    Logger.info("Application attaches WebSocket")
    Session.requestSessionID(request) match {

      case sid => Future.successful {  Right(ActorFlow.actorRef(WebSocketActor.props(sid, jobManager))) }
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
    val user_o : Option[User] = Session.getUser(session_id)

    Ok(views.html.main(webJarAssets, views.html.general.maincontent(),"Home", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }
  }

  def login = Action { implicit request =>

    val session_id = Session.requestSessionID(request)
    val user_o : Option[User] = Session.getUser(session_id)

    Ok(views.html.backend.login(webJarAssets, "Backend", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }

  }


  def backend = Action { implicit request =>

    val session_id = Session.requestSessionID(request)
    val user_o : Option[User] = Session.getUser(session_id)

    //TODO add auth validation here for allowing access to the dashboard

    if(!request.headers.get("referer").getOrElse("").equals("http://" + request.host + "/login")) {

        InternalServerError(views.html.errors.pagenotfound())

    }


    else {
    Ok(views.html.backend.backend(webJarAssets, "Backend", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
      }
    }

  }



  def forbidden = {

      InternalServerError(views.html.errors.pagenotfound())

  }



  def contact(title: String = "Contact") = Action { implicit request =>

    Ok(views.html.general.contact()).withSession {

      Session.closeSessionRequest(request, Session.requestSessionID(request)) // Send Session Cookie
    }

  }



  // Route is handled by Mithril
  def showTool(toolname: String) = Action { implicit request =>

    Redirect(s"/#/tools/$toolname")
  }


  def showJob(job_id : String) = Action { implicit request =>

    Redirect(s"/#/jobs/$job_id")
  }


  def static(static : String) = Action { implicit request =>


    Redirect(s"/#/$static")
  }


  /*
    *  Return the Input form of the corresponding tool
    */
  // TODO Replace via reflection
  def form(toolname: String) = Action { implicit request =>

    val toolframe : Html = toolname match {
      case "alnviz" => views.html.alnviz.form(Alnviz.inputForm)
      case "tcoffee" => views.html.tcoffee.form(Tcoffee.inputForm)
      case "hmmer3" => views.html.hmmer3.form(Hmmer3.inputForm)
      case "psiblast" => views.html.psiblast.form(Psiblast.inputForm)
      case "mafft" => views.html.mafft.form(Mafft.inputForm)
      case "csblast" => views.html.csblast.form(Csblast.inputForm)
      case "hhpred" => views.html.hhpred.form(HHpred.inputForm)
      case "reformatb" => views.html.reformatb.form(Reformatb.inputForm)
      case "clans" => views.html.clans.form(Clans.inputForm)
    }

    Ok(views.html.general.submit(toolname, toolframe, None)).withSession {

      Session.closeSessionRequest(request, Session.requestSessionID(request)) // Send Session Cookie
    }
  }

  /**
   * Allows to access result files by the filename and a given jobID
   */
  def file(filename : String, jobID : String) = Action{ implicit request =>

    val session_id = Session.requestSessionID(request)

    // main_id exists, allow send File

    Ok.sendFile(new java.io.File(s"$jobPath$SEP$jobID${SEP}results$SEP$filename"))
      .withHeaders(CONTENT_TYPE->"text/plain").withSession { Session.closeSessionRequest(request, session_id)
    }
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("picture").map { picture =>
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      picture.ref.moveTo(new File(s"/tmp/picture/$filename"))
      Ok("File uploaded")
    }.getOrElse {
      Redirect(s"/#/upload").flashing(
        "error" -> "Missing file")
    }
  }

}