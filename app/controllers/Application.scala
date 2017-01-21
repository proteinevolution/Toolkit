package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.WebSocketActor
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream.Materializer
import models.search.JobDAO
import models.{Constants, Values}
import modules.tel.TEL
import modules.{CommonModule, LocationProvider}
import modules.tel.env.Env
import play.api.Configuration
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
final class Application @Inject()(webJarAssets                                    : WebJarAssets,
                                  val messagesApi                                 : MessagesApi,
                                  val values                                      : Values,
                                  webSocketActorFactory                           : WebSocketActor.Factory,
                                  @NamedCache("userCache") implicit val userCache : CacheApi,
                                  implicit val locationProvider                   : LocationProvider,
                                  @NamedCache("viewCache") val viewCache          : CacheApi,
                                  val jobDao                                      : JobDAO,
                                  val reactiveMongoApi                            : ReactiveMongoApi,
                                  system                                          : ActorSystem,
                                  mat                                             : Materializer,
                                  val tel                                         : TEL,
                                  val env                                         : Env,
                                  val search                                      : Search,
                                  val settings                                    : Settings,
                                  @Named("master") master                         : ActorRef,
                                  configuration                                   : Configuration)
                                  extends Controller with I18nSupport with CommonModule
                                                                      with Constants
                                                                      with UserSessions {

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  val SID = "sid"


  def ws : WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>
    getUser.map { user =>
      Right(ActorFlow.actorRef((out) => Props(webSocketActorFactory(user.userID, out))))
    }
  }


  /**
    * Handles the request of the index page of the toolkit. This will assign a session to the User if
    * not already present.
    * Currently the index controller will assign a session id to the user for identification purpose.
    */
  def index : Action[AnyContent] = Action.async { implicit request =>

    val port = request.host.slice(request.host.indexOf(":")+1,request.host.length)
    val hostname = request.host.slice(0, request.host.indexOf(":"))
    env.configure("PORT", port)
    env.configure("HOSTNAME", hostname)

    tel.port = request.host.slice(request.host.indexOf(":")+1,request.host.length)
    tel.hostname = hostname
    println("[CONFIG:] running on port "+tel.port)
    println("[CONFIG:] execution mode: "+settings.clusterMode)
    getUser.map { user =>
      Ok(views.html.main(webJarAssets, user))
        .withSession(sessionCookie(request, user.sessionID.get))
    }
  }



  // Route is handled by Mithril
  def showTool(toolName: String) = Action { implicit request =>

    Redirect(s"/#/tools/$toolName")
  }


  def showJob(idString : String) : Action[AnyContent] = Action.async { implicit request =>
    BSONObjectID.parse(idString).toOption match {
      case Some(mainID) =>
        Future.successful(Redirect(s"/#/jobs/${mainID.stringify}"))
      case None =>
        jobDao.fuzzySearchJobID(idString).map { hit =>
          hit.getHits.getHits.headOption match {
            case Some(jobHit) =>
              Redirect(s"/#/jobs/${jobHit.getId}")
            case None =>
              NotFound
          }
        }
    }
  }


  def static(static : String) = Action { implicit request =>
    Redirect(s"/#/$static")
  }


  /**
   * Allows to access resultpanel files by the filename and a given jobID
   */
  def file(filename : String, mainID : String) : Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>

      // mainID exists, allow send File

      Ok.sendFile(new java.io.File(s"$jobPath$SEPARATOR$mainID${SEPARATOR}results$SEPARATOR$filename"))
        .withSession(sessionCookie(request, user.sessionID.get))
        .as("text/plain") //TODO Only text/plain for files currently supported
    }
  }



  def upload : Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { picture =>
      // TODO: Handle file storage, pass uploaded sequences to model, validate uploaded files
      import java.io.File
      val filename = picture.filename
      val contentType = picture.contentType
      println(picture)
      picture.ref.moveTo(new File(s"/tmp/$filename"))
      Ok("File uploaded")
    }.getOrElse {
      Redirect(s"/upload").flashing(
        "error" -> "Missing file")
    }
  }
}





