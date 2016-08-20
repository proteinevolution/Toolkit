package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.WebSocketActor
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import models.database.{Session, SessionData, User}
import models.tel.TEL
import models.tools._
import modules.common.HTTPRequest
import org.joda.time.DateTime
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.{Configuration, Logger}
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDateTime, BSONDocument}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._


@Singleton
class Application @Inject()(webJarAssets: WebJarAssets,
                            val messagesApi: MessagesApi,
                            val reactiveMongoApi : ReactiveMongoApi,
                            system: ActorSystem,
                            mat: Materializer,
                            val tel : TEL,
                            @Named("userManager") userManager : ActorRef,    // Connect to JobManager
                            configuration: Configuration) extends Controller with I18nSupport
                                                                             with ReactiveMongoComponents
                                                                             with Common
                                                                             with Session {

  val SEP = java.io.File.separator

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  implicit val timeout = Timeout(5.seconds)

  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"

  // get the collection 'users'
  def userCollection = reactiveMongoApi.database.map(_.collection("users").as[BSONCollection](FailoverStrategy()))

  /**
    * Opens the websocket
    *
    * @return
    */
  def ws = WebSocket.accept[JsValue, JsValue] { implicit request =>
    Logger.info("Application attaches WebSocket")
    ActorFlow.actorRef(WebSocketActor.props(getUser, userManager))
  }


  /**
    * Handles the request of the index page of the toolkit. This will assign a session to the User if
    * not already present.
    * Currently the index controller will assign a session id to the user for identification purpose.
    */
  def index = Action.async { implicit request =>
    val sessionID      = requestSessionID
    val httpRequest    = HTTPRequest(request)
    val newSessionData = SessionData(ip        = request.remoteAddress,
                                     userAgent = httpRequest.userAgent.getOrElse("Not Specified"),
                                     location  = geoIP.getLocation,
                                     online    = true)

    Logger.info(newSessionData.toString)

    userCollection.flatMap(_.find(BSONDocument(User.SESSIONID -> sessionID)).one[User]).map {
      case Some(user)   =>
        val selector = BSONDocument(User.IDDB          -> user.userID)
        val modifier = BSONDocument("$set"             ->
                       BSONDocument(User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)),
                                    "$addToSet"        ->
                       BSONDocument(User.SESSIONDATA   -> newSessionData))

        userCollection.flatMap(_.update(selector,modifier))
        addUser(sessionID, user)
        Ok(views.html.main(webJarAssets, views.html.general.maincontent(), "Home", user)).withSession {
          closeSessionRequest(request, sessionID)
        }

      case None =>
        val newUser = getUser
        userCollection.flatMap(_.insert(newUser))
        Ok(views.html.main(webJarAssets, views.html.general.maincontent(), "Home", newUser)).withSession {
          closeSessionRequest(request, sessionID)
        }
    }
  }


  def contact(title: String = "Contact") = Action { implicit request =>

    Ok(views.html.general.contact()).withSession {

      closeSessionRequest(request, requestSessionID) // Send Session Cookie
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
      case "hmmer3"     => views.html.tools.forms.hmmer3(tel, Hmmer3.inputForm)
      case "psiblast"   => views.html.tools.forms.psiblast(tel, Psiblast.inputForm.fill(("", "", "", 1, 10, 11, 1, 200, "")))
      case "mafft"      => views.html.tools.forms.mafft(Mafft.inputForm)
      case "csblast"    => views.html.tools.forms.csblast(tel, Csblast.inputForm)
      case "hhpred"     => views.html.tools.forms.hhpred(tel, HHpred.inputForm)
      case "hhblits"    => views.html.tools.forms.hhblits(tel, HHblits.inputForm)
      case "reformatb"  => views.html.tools.forms.reformatb(Reformatb.inputForm)
      case "clans"      => views.html.tools.forms.clans(tel, Clans.inputForm)
    }

    Ok(views.html.general.submit(tel, toolName, toolFrame, None)).withSession {

      closeSessionRequest(request, requestSessionID) // Send Session Cookie
    }
  }

  /**
   * Allows to access result files by the filename and a given jobID
   */
  def file(filename : String, jobID : String) = Action{ implicit request =>

    val sessionID = requestSessionID

    // main_id exists, allow send File

    Ok.sendFile(new java.io.File(s"$jobPath$SEP$jobID${SEP}results$SEP$filename"))
      .withSession { closeSessionRequest(request, sessionID)}
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





