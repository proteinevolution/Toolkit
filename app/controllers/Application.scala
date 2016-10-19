package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.WebSocketActor
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import models.{Constants, Values}
import models.tel.TEL
import models.tools.ToolModel
import modules.Common
import modules.tools.ToolMatcher
import play.api.Configuration
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi

import scala.concurrent.ExecutionContext.Implicits.global
import modules.tools.ToolMirror


@Singleton
class Application @Inject()(webJarAssets     : WebJarAssets,
                        val messagesApi      : MessagesApi,
                            final val values : Values,
@NamedCache("userCache") implicit val userCache : CacheApi,
@NamedCache("viewCache") val viewCache: CacheApi,
                        val reactiveMongoApi : ReactiveMongoApi,
                            system           : ActorSystem,
                            mat              : Materializer,
                        val tel              : TEL,
                        val toolMatcher      : ToolMatcher,
                        val search           : Search,
                        val toolMirror       : ToolMirror,
      @Named("userManager") userManager      : ActorRef,    // Connect to JobManager
                            configuration    : Configuration) extends Controller with I18nSupport
                                                                                 with Common
                                                                                 with Constants
                                                                                 with UserSessions {

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  val SID = "sid"


  /**
    * Opens the websocket
    *
    * @return
    */
  def ws = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>
    getUser.map { user =>
      Right(ActorFlow.actorRef(WebSocketActor.props(user.userID, userManager)))
    }
  }


  /**
    * Handles the request of the index page of the toolkit. This will assign a session to the User if
    * not already present.
    * Currently the index controller will assign a session id to the user for identification purpose.
    */
  def index = Action.async { implicit request =>

    tel.port = request.host.slice(request.host.indexOf(":")+1,request.host.length)
    getUser.map { user =>
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


  /**
   * Allows to access resultpanel files by the filename and a given jobID
   */
  def file(filename : String, mainID : String) = Action.async { implicit request =>
    getUser.map { user =>

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





