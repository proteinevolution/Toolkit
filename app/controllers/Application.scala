package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.WebSocketActor
import akka.actor.{ActorRef, ActorSystem}
import akka.stream.Materializer
import akka.util.Timeout
import models.Constants
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
import scala.concurrent.duration._
import modules.tools.ToolMirror
import play.api.data.Forms._
import play.api.data.{Form, _}


@Singleton
class Application @Inject()(webJarAssets     : WebJarAssets,
                        val messagesApi      : MessagesApi,
@NamedCache("userCache") implicit val userCache : CacheApi,
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
  implicit val timeout = Timeout(5.seconds)
  val SID = "sid"

  /*
    Template Form for all Tools
   */
  val jobForm = Form(
    tuple(
      "alignment" -> nonEmptyText,
      "alignment_format" -> text,
      "standarddb" -> text,
      "matrix" -> text,
      "num_iter" -> number,
      "evalue" -> number,
      "gap_open" -> number,
      "gap_ext" -> number,
      "desc" -> number
    )
  )


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


    //toolMirror.invokeToolName("alnviz") // even though this is a runtime mirror, it seems fast enough
    //toolMirror.listToolModels() // faster than clapper
    println(ToolModel.values) // even faster than the scala api
    //toolMirror.findInstances() // finds all tool instances in the models package but this seems to be rather slow: alternatives are macros or sealed trait enumeration
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

  def form(toolname : String) = Action { implicit request =>

    //val toolFrame = toolMatcher.matcher(toolname)
    val toolframe = toolname

    Ok(views.html.jobs.main(Map("foo" -> "bar", "baz" -> "goo")))
  }


  // Old input form with submit view
  /*
  def form(toolName: String) = Action.async { implicit request =>
    getUser.map{ user =>
      //val toolFrame = toolMatcher.loadTemplate(toolName)
      val toolFrame = toolMatcher.matcher(toolName)
      Ok(views.html.general.submit(tel, toolName, toolFrame, None))
    }
  }
  */

  /**
   * Allows to access result files by the filename and a given jobID
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





