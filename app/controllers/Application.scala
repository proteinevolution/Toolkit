package controllers

import javax.inject.{Inject, Singleton}

import actors.WebSocketActor
import akka.actor.{ActorSystem, Props}
import models.sge.Cluster
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import models.database.statistics.ToolStatistic
import models.search.JobDAO
import models.Constants
import models.results.BlastVisualization
import models.tools.ToolFactory
import modules.tel.TEL
import modules.{CommonModule, LocationProvider}
import modules.tel.env.Env
import play.api.{Configuration, Logger}
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.Files
import play.api.libs.json.{JsValue, Json}
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
final class Application @Inject()(webJarAssets: WebJarAssets,
                                  val messagesApi: MessagesApi,
                                  webSocketActorFactory: WebSocketActor.Factory,
                                  @NamedCache("userCache") implicit val userCache: CacheApi,
                                  implicit val locationProvider: LocationProvider,
                                  @NamedCache("viewCache") val viewCache: CacheApi,
                                  toolFactory: ToolFactory,
                                  val jobDao: JobDAO,
                                  val reactiveMongoApi: ReactiveMongoApi,
                                  system: ActorSystem,
                                  mat: Materializer,
                                  val tel: TEL,
                                  val env: Env,
                                  val cluster: Cluster,
                                  val search: Search,
                                  val settings: Settings,
                                  configuration: Configuration)
    extends Controller
    with I18nSupport
    with CommonModule
    with Constants
    with UserSessions {

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem   = system
  // Use a direct reference to SLF4J
  private val logger = org.slf4j.LoggerFactory.getLogger("controllers.Application")
  val SID            = "sid"

  // Run this once to generate database objects for the statistics
  def generateStatisticsDB(): Unit = {
    for (toolName: String <- toolFactory.values.keys) {
      addStatistic(ToolStatistic(BSONObjectID.generate(), toolName, 0, 0, List.empty, List.empty, List.empty))
    }
  }

  /**
    * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
    * Future[Flow], which is required internally.
    *
    * @return a fully realized websocket.
    */
  def ws: WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {

    case rh if sameOriginCheck(rh) =>
      getUser(rh)
        .map { user =>
          Right(ActorFlow.actorRef((out) => Props(webSocketActorFactory(user.sessionID.get, out))))
        }
        .recover {
          case e: Exception =>
            logger.error("Cannot create websocket", e)
            val jsError = Json.obj("error" -> "Cannot create websocket")
            val result  = InternalServerError(jsError)
            Left(result)
        }

    case rejected =>
      logger.error(s"Request $rejected failed same origin check")
      Future.successful {
        Left(Forbidden("forbidden"))
      }
  }

  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  def sameOriginCheck(rh: RequestHeader): Boolean = {
    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        logger.error(
          s"originCheck: rejecting request because Origin header value $badOrigin is not in the same origin")
        false

      case None =>
        logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  def originMatches(origin: String): Boolean = {
    origin.contains(TEL.hostname + ":" + TEL.port) || origin.contains("tuebingen.mpg.de")
  }

  /**
    * Handles the request of the index page of the toolkit. This will assign a session to the User if
    * not already present.
    * Currently the index controller will assign a session id to the user for identification purpose.
    */
  def index(message: String = ""): Action[AnyContent] = Action.async { implicit request =>
    //generateStatisticsDB

    ConfigFactory.load().getString(s"toolkit_mode") match {

      case "prod" =>
        val port     = "9000"
        val hostname = "rye"
        env.configure("PORT", port)
        env.configure("HOSTNAME", hostname)
        TEL.port = port
        TEL.hostname = hostname
        println("[CONFIG:] running on port " + TEL.port)
        println("[CONFIG:] execution mode: " + settings.clusterMode)

      case _ =>
        val port     = request.host.slice(request.host.indexOf(":") + 1, request.host.length)
        val hostname = request.host.slice(0, request.host.indexOf(":"))
        env.configure("PORT", port)
        env.configure("HOSTNAME", hostname)
        TEL.port = port
        TEL.hostname = hostname
        println("[CONFIG:] running on port " + TEL.port)
        println("[CONFIG:] execution mode: " + settings.clusterMode)

    }

    getUser.map { user =>
      Logger.info(user.toString)
      Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong), message))
        .withSession(sessionCookie(request, user.sessionID.get, Some(user.getUserData.nameLogin)))
    }
  }

  // Routes are handled by Mithril, redirect.
  def showTool(toolName: String): Action[AnyContent] = Action { implicit request =>
    Redirect(s"/#/tools/$toolName")
  }

  def showJob(idString: String): Action[AnyContent] = Action { implicit request =>
    Redirect(s"/#/jobs/$idString")
  }

  def static(static: String): Action[AnyContent] = Action { implicit request =>
    Redirect(s"/#/$static")
  }

  /**
    * Allows to access resultpanel files by the filename and a given jobID
    */
  def file(filename: String, mainID: String): Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>
      // mainID exists, allow send File
      if (new java.io.File(s"$jobPath$SEPARATOR$mainID${SEPARATOR}results$SEPARATOR$filename").exists)
        Ok.sendFile(new java.io.File(s"$jobPath$SEPARATOR$mainID${SEPARATOR}results$SEPARATOR$filename"))
          .withSession(sessionCookie(request, user.sessionID.get, Some(user.getUserData.nameLogin)))
          .as("text/plain") //TODO Only text/plain for files currently supported
      else
        Ok // TODO This needs more case validations

    }
  }

  def getStructureFile(filename: String): Action[AnyContent] = Action.async { implicit request =>
    {

      val db = BlastVisualization.identifyDatabase(filename.replaceAll("(.cif)|(.pdb)", ""))
      val filepath = db match {
        case "scop" =>
          env.get("SCOPE")
        case "mmcif" =>
          env.get("CIF")
      }
      Future.successful(Ok.sendFile(new java.io.File(s"$filepath$SEPARATOR$filename")).as("text/plain"))
    }
  }

  def upload: Action[MultipartFormData[Files.TemporaryFile]] = Action(parse.multipartFormData) { request =>
    request.body
      .file("file")
      .map { file =>
        // TODO: Handle file storage, pass uploaded sequences to model, validate uploaded files
        Logger.info("Uploading file.")
        import java.io.File
        val filename    = file.filename
        val contentType = file.contentType
        println(file)
        file.ref.moveTo(new File(s"/tmp/$filename"))
        Ok("File uploaded")
      }
      .getOrElse {
        Redirect(s"/upload").flashing("error" -> "Missing file")
      }
  }

  def javascriptRoutes: Action[AnyContent] = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.Jobs.updateDateViewed,
        routes.javascript.Tool.frontendCount,
        routes.javascript.JobController.create,
        routes.javascript.JobController.check,
        routes.javascript.JobController.delete,
        routes.javascript.Jobs.annotation,
        routes.javascript.DataController.get,
        routes.javascript.DataController.writeArticle,
        routes.javascript.DataController.getRecentArticles,
        routes.javascript.Auth.getUserData,
        routes.javascript.Auth.signInSubmit,
        routes.javascript.Auth.signUpSubmit,
        routes.javascript.Auth.verification,
        routes.javascript.Auth.profileSubmit,
        routes.javascript.Auth.passwordChangeSubmit,
        routes.javascript.Auth.resetPassword,
        routes.javascript.Auth.resetPasswordChange,
        routes.javascript.HmmerController.aln,
        routes.javascript.HmmerController.alnEval,
        routes.javascript.HmmerController.full,
        routes.javascript.HmmerController.evalFull,
        routes.javascript.PSIBlastController.aln,
        routes.javascript.PSIBlastController.alnEval,
        routes.javascript.PSIBlastController.full,
        routes.javascript.PSIBlastController.evalFull,
        routes.javascript.HHblitsController.aln,
        routes.javascript.HHblitsController.alnEval,
        routes.javascript.HHblitsController.full,
        routes.javascript.HHblitsController.evalFull,
        routes.javascript.HHpredController.aln,
        routes.javascript.HHpredController.alnEval,
        routes.javascript.PSIBlastController.loadHits,
        routes.javascript.HmmerController.loadHits,
        routes.javascript.HHblitsController.loadHits,
        routes.javascript.HHpredController.loadHits,
        routes.javascript.AlignmentController.loadHits,
        routes.javascript.AlignmentController.getAln,
        routes.javascript.Application.ws
      )
    ).as("text/javascript")
  }

}
