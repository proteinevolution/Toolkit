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
import javax.inject.{Inject, Singleton}

import models.sessions.Session
import models.tools.{Alnviz, Hmmer3, Psiblast, Tcoffee}
import actors.MasterConnection
import io.prismic._

import scala.concurrent.duration._
import scala.concurrent.Future
import play.api.mvc._
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._


object Application {

  // -- Resolve links to documents
  def linkResolver(api: Api)(implicit request: RequestHeader) = DocumentLinkResolver(api) {
    case (docLink, maybeBookmarked) if !docLink.isBroken => routes.Application.detail(docLink.id, docLink.slug).absoluteURL()
    case _ => routes.Application.brokenLink().absoluteURL()
  }
}


@Singleton
class Application @Inject()(webJarAssets: WebJarAssets,
                            val messagesApi: MessagesApi,
                            system: ActorSystem,
                            masterConnection : MasterConnection,
                            mat: Materializer,
                            configuration: Configuration) extends Controller with I18nSupport {


  val SEP = java.io.File.separator
  val user_id = 12345  // TODO integrate user_id


  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem = system
  implicit val timeout = Timeout(5.seconds)

  val jobPath = s"${ConfigFactory.load().getString("job_path")}$SEP"

  private val Cache = BuiltInCache(200)

  // -- Write debug and error messages to the Play `prismic` logger (check the configuration in application.conf)
  private val Logger2 = (level: Symbol, message: String) => level match {
    case 'DEBUG => play.api.Logger("prismic").debug(message)
    case 'ERROR => play.api.Logger("prismic").error(message)
    case _      => play.api.Logger("prismic").info(message)
  }

  private def endpoint = configuration.getString("prismic.api").getOrElse(sys.error(s"Missing configuration prismic.api"))
  private def token = configuration.getString("prismic.token")
  private def fetchApi: Future[Api] = Api.get(endpoint, accessToken = token, cache = Cache, logger = Logger2)

  private def ref(api: Api)(implicit request: RequestHeader): String = {
    val experimentRef: Option[String] = request.cookies.get(Prismic.experimentsCookie).map(_.value).flatMap(api.experiments.refFromCookie)
    val previewRef: Option[String] = request.cookies.get(Prismic.previewCookie).map(_.value)
    previewRef.orElse(experimentRef).getOrElse(api.master.ref)
  }


  def ws = WebSocket.acceptOrResult[JsValue, JsValue] { implicit request =>

    Logger.info("Application attaches WebSocket")
    Session.requestSessionID(request) match {

      case sid => Future.successful {  Right(ActorFlow.actorRef(WebSocketActor.props(sid, masterConnection.masterProxy))) }
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

  // -- Page not found
  def PageNotFound = NotFound(views.html.pageNotFound())

  def brokenLink = Action { implicit request =>
    PageNotFound
  }

 // -- Home page
  def home(page: Int) = Action.async { implicit request =>

    for {
      api <- fetchApi
      response <- api.forms("everything").ref(ref(api)).pageSize(10).page(page).submit()
    } yield {

      val session_id = Session.requestSessionID(request)
      Ok(views.html.main(webJarAssets, views.html.general.newcontent(), "Home")).withSession {
        Session.closeSessionRequest(request, session_id)
      }
    }
  }

  def contact(title: String = "Contact") = Action { implicit request =>

    Ok(views.html.general.contact()).withSession {

      Session.closeSessionRequest(request, Session.requestSessionID(request)) // Send Session Cookie
    }

  }

  // -- Document detail
  def detail(id: String, slug: String) = Action.async { implicit request =>
    for {
      api <- fetchApi
      maybeDocument <- api.forms("everything").ref(ref(api)).pageSize(1).submit().map(_.results.headOption)
    } yield {
      checkSlug(maybeDocument, slug) {
        case Left(newSlug)   => MovedPermanently(routes.Application.detail(id, newSlug).url)
        case Right(document) => Ok(views.html.detail(document, api))
      }
    }
  }

  // -- Basic Search
  def search(q: Option[String], page: Int) = Action.async { implicit request =>
    for {
      api <- fetchApi
      response <- api.forms("everything").query(Predicate.fulltext("document", q.getOrElse(""))).ref(ref(api)).pageSize(10).page(page).submit()
    } yield {
      Ok(views.html.search(q, response, api))
    }
  }

  // -- Preview Action
  def preview(token: String) = Action.async { implicit req =>
    for {
      api <- fetchApi
      redirectUrl <- api.previewSession(token, Application.linkResolver(api), routes.Application.home().url)
    } yield {
      Redirect(redirectUrl).withCookies(Cookie(Prismic.previewCookie, token, path = "/", maxAge = Some(30 * 60 * 1000), httpOnly = false))
    }
  }

  // -- Helper: Check if the slug is valid and redirect to the most recent version id needed
  def checkSlug(document: Option[Document], slug: String)(callback: Either[String, Document] => Result) =
    document.collect {
      case document if document.slug == slug         => callback(Right(document))
      case document if document.slugs.contains(slug) => callback(Left(document.slug))
    }.getOrElse {
      Results.NotFound(views.html.pageNotFound())
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
      case "psiblast" => views.html.psiblast.form(Psiblast.inputForm)
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