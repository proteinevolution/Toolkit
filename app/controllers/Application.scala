package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.WebSocketActor
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import models.Constants
import models.tel.TEL
import modules.tools.ToolMatcher
import play.api.Configuration
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import modules.tools.ReflectionDAO


@Singleton
class Application @Inject()(webJarAssets     : WebJarAssets,
                        val messagesApi      : MessagesApi,
   @NamedCache("userCache") userCache        : CacheApi,
                        val reactiveMongoApi : ReactiveMongoApi,
                            system           : ActorSystem,
                            mat              : Materializer,
                        val tel              : TEL,
                        val toolMatcher      : ToolMatcher,
                        val search           : Search,
                        val reflectionDao : ReflectionDAO,
      @Named("userManager") userManager      : ActorRef,    // Connect to JobManager
                            configuration    : Configuration) extends Controller with I18nSupport
                                                                                 with ReactiveMongoComponents
                                                                                 with Common
                                                                                 with Constants
                                                                                 with UserSessions {

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  implicit val timeout = Timeout(5.seconds)
  val SID = "sid"
  // get the collection 'users'
  def userCollection = reactiveMongoApi.database.map(_.collection("users").as[BSONCollection](FailoverStrategy()))

  // TODO get this function into a trait if possible

  /**
    * Opens the websocket
    *
    * @return
    */
  def ws = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      Right(ActorFlow.actorRef(WebSocketActor.props(user.userID, userManager)))
    }
  }


  /**
    * Handles the request of the index page of the toolkit. This will assign a session to the User if
    * not already present.
    * Currently the index controller will assign a session id to the user for identification purpose.
    */
  def index = Action.async { implicit request =>

    reflectionDao.invokeToolName("alnviz")
    getUser(request, userCollection, userCache).map { user =>
      Ok(views.html.main(webJarAssets, views.html.general.maincontent(), "Home", user))
        .withSession(sessionCookie(request, user.sessionID.get))
    }
  }


  def contact(title: String = "Contact") = Action { implicit request =>
    Ok(views.html.general.contact())
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
  def form(toolName: String) = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map{ user =>
      val toolFrame = toolMatcher.matcher(toolName)

      Ok(views.html.general.submit(tel, toolName, toolFrame, None))
    }
  }

  /**
   * Allows to access result files by the filename and a given jobID
   */
  def file(filename : String, mainID : String) = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>

      // mainID exists, allow send File

      Ok.sendFile(new java.io.File(s"$jobPath$SEPARATOR$mainID${SEPARATOR}results$SEPARATOR$filename"))
        .withSession(sessionCookie(request, user.sessionID.get))
        .as("text/plain") //TODO Only text/plain for files currently supported
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





