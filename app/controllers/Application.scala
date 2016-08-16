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

import scala.concurrent.duration._
import play.api.mvc._
import play.api.Configuration


@Singleton
class Application @Inject()(webJarAssets: WebJarAssets,
                            val messagesApi: MessagesApi,
                            system: ActorSystem,
                            mat: Materializer,
                            @Named("jobManager") jobManager : ActorRef,    // Connect to JobManager
                            configuration: Configuration) extends Controller with I18nSupport
                                                                             with Common {

  val SEP = java.io.File.separator

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  implicit val timeout = Timeout(5.seconds)

  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"


  /**
    * Opens the websocket
    * @return
    */
  def ws = WebSocket.accept[JsValue, JsValue] { implicit request =>
    Logger.info("Application attaches WebSocket")
    ActorFlow.actorRef(WebSocketActor.props(Session.requestSessionID, jobManager))
  }


  /**
    * Handles the request of the index page of the toolkit. This will assign a session to the User if
    * not already present.
    * Currently the index controller will assign a session id to the user for identification purpose.
    */
  def index = Action { implicit request =>

    val sessionID = Session.requestSessionID
    val user : Option[User] = Session.getUser

    Logger.info(geoIP.getLocation.toString)

    Ok(views.html.main(webJarAssets, views.html.general.maincontent(),"Home", user)).withSession {
      Session.closeSessionRequest(request, sessionID)
    }
  }


  def contact(title: String = "Contact") = Action { implicit request =>

    Ok(views.html.general.contact()).withSession {

      Session.closeSessionRequest(request, Session.requestSessionID) // Send Session Cookie
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
  def form(toolName: String) = Action { implicit request =>
    val toolFrame = toolName match {
      case "alnviz"     => views.html.tools.forms.alnviz(Alnviz.inputForm)
      case "tcoffee"    => views.html.tools.forms.tcoffee(Tcoffee.inputForm)
      case "hmmer3"     => views.html.tools.forms.hmmer3(Hmmer3.inputForm)
      case "psiblast"   => views.html.tools.forms.psiblast(Psiblast.inputForm.fill(("", "", "", 1, 10, 11, 1, 200, "")))
      case "mafft"      => views.html.tools.forms.mafft(Mafft.inputForm)
      case "csblast"    => views.html.tools.forms.csblast(Csblast.inputForm)
      case "hhpred"     => views.html.tools.forms.hhpred(HHpred.inputForm)
      case "hhblits"    => views.html.tools.forms.hhblits(HHblits.inputForm)
      case "reformatb"  => views.html.tools.forms.reformatb(Reformatb.inputForm)
      case "clans"      => views.html.tools.forms.clans(Clans.inputForm)
    }

    Ok(views.html.general.submit(toolName, toolFrame, None)).withSession {

      Session.closeSessionRequest(request, Session.requestSessionID) // Send Session Cookie
    }
  }

  /**
   * Allows to access result files by the filename and a given jobID
   */
  def file(filename : String, jobID : String) = Action{ implicit request =>

    val sessionID = Session.requestSessionID

    // main_id exists, allow send File

    Ok.sendFile(new java.io.File(s"$jobPath$SEP$jobID${SEP}results$SEP$filename"))
      .withSession { Session.closeSessionRequest(request, sessionID)}
      .as("text/plain")   //TODO Only text/plain for files currently supported
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

/* Example RESTFUL  Actions

public static void createUser(User newUser) {
    newUser.save();
    user(newUser.id);
}

public static void updateUser(Long id, User user) {
    User dbUser = User.findById(id);
    dbUser.updateDetails(user); // some model logic you would write to do a safe merge
    dbUser.save();
    user(id);
}

public static void deleteUser(Long id) {
    User.findById(id).delete();
    renderText("success");
}

public static void user(Long id)  {
    User user = User.findById(id)
    render(user);
}

 */





