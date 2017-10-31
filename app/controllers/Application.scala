package controllers

import java.net.InetAddress
import javax.inject.{ Inject, Named, Singleton }

import actors.ClusterMonitor.Multicast
import actors.WebSocketActor
import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.results.Common
import de.proteinevolution.models.search.JobDAO
import de.proteinevolution.models.sge.Cluster
import models.tools.ToolFactory
import models.UserSessions
import de.proteinevolution.common.{ HTTPRequest, LocationProvider }
import de.proteinevolution.db.MongoStore
import de.proteinevolution.tel.TEL
import de.proteinevolution.tel.env.Env
import play.api.cache._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.{ JsValue, Json }
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.api.routing.JavaScriptReverseRouter
import play.api.{ Configuration, Environment, Logger }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONDocument
import org.webjars.play.WebJarsUtil
import com.redfin.sitemapgenerator.{ ChangeFreq, WebSitemapGenerator, WebSitemapUrl }
import de.proteinevolution.models.Constants
import play.api.Mode.Test

import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }

@Singleton
final class Application @Inject()(webJarsUtil: WebJarsUtil,
                                  messagesApi: MessagesApi,
                                  @Named("clusterMonitor") clusterMonitor: ActorRef,
                                  webSocketActorFactory: WebSocketActor.Factory,
                                  @NamedCache("userCache") implicit val userCache: SyncCacheApi,
                                  implicit val locationProvider: LocationProvider,
                                  val reactiveMongoApi: ReactiveMongoApi,
                                  toolFactory: ToolFactory,
                                  val jobDao: JobDAO,
                                  mongoStore: MongoStore,
                                  system: ActorSystem,
                                  userSessions: UserSessions,
                                  mat: Materializer,
                                  val tel: TEL,
                                  val env: Env,
                                  val cluster: Cluster,
                                  val search: Search,
                                  val settings: Settings,
                                  configuration: Configuration,
                                  constants: Constants,
                                  cc: ControllerComponents,
                                  environment: Environment)
    extends AbstractController(cc)
    with I18nSupport
    with CommonController {

  private val toolkitMode = ConfigFactory.load().getString(s"toolkit_mode")

  implicit val implicitMaterializer: Materializer = mat
  implicit val implicitActorSystem: ActorSystem   = system
  // Use a direct reference to SLF4J
  private val logger = org.slf4j.LoggerFactory.getLogger("controllers.Application")
  val SID            = "sid"

  private[this] val blacklist = ConfigFactory.load().getStringList("banned.ip")

  /**
   * Creates a websocket.  `acceptOrResult` is preferable here because it returns a
   * Future[Flow], which is required internally.
   *
   * @return a fully realized websocket.
   */
  def ws: WebSocket = WebSocket.acceptOrResult[JsValue, JsValue] {

    case rh if sameOriginCheck(rh) =>
      println("Creating new WebSocket. ip: " + rh.remoteAddress.toString + ", with sessionId: " + rh.session)

      userSessions
        .getUser(rh)
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
        Left(Ok("forbidden"))
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

    if (environment.mode == play.api.Mode.Test)
      true
    else {
      rh.headers.get("Origin") match {
        case Some(originValue)
            if originMatches(originValue) && !HTTPRequest(rh).isBot(rh) && !blacklist.contains(rh.remoteAddress) =>
          logger.debug(s"originCheck: originValue = $originValue")
          true

        case Some(badOrigin) =>
          logger.error(
            s"originCheck: rejecting request because Origin header value $badOrigin is not in the same origin"
          )
          false

        case None =>
          logger.error("originCheck: rejecting request because no Origin header found")
          false
      }
    }
  }

  /**
   * Returns true if the value of the Origin header contains an acceptable value.
   */
  def originMatches(origin: String): Boolean = {
    origin.contains(TEL.hostname + ":" + TEL.port) || origin.contains("tuebingen.mpg.de") || origin.contains(
      "tue.mpg.de"
    )
  }

  /**
   * Handles the request of the index page of the toolkit. This will assign a session to the User if
   * not already present.
   * Currently the index controller will assign a session id to the user for identification purpose.
   */
  def index(message: String = ""): Action[AnyContent] = Action.async { implicit request =>
    //generateStatisticsDB

    toolkitMode match {

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
        if (!hostname.startsWith("olt")) {
          env.configure("headLessMode", "true")
        }
        env.configure("HOSTNAME", "olt")
        TEL.port = port
        TEL.hostname = hostname
        println("[CONFIG:] running on port " + TEL.port)
        println("[CONFIG:] execution mode: " + settings.clusterMode)

    }

    userSessions.getUser.map { user =>
      Logger.info(InetAddress.getLocalHost.getHostName + "\n" + user.toString)
      Ok(views.html.main(webJarsUtil, toolFactory.values.values.toSeq.sortBy(_.toolNameLong), message))
        .withSession(userSessions.sessionCookie(request, user.sessionID.get))
    }
  }

  // why is this needed?
  def sitemapGenerator: Action[AnyContent] = Action { implicit request =>
    val wsg =
      WebSitemapGenerator.builder("https://toolkit.tuebingen.mpg.de", environment.getFile("public/")).gzip(true).build

    // add pages here
    val pages = List(
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/hhblits", "priority" -> 1.0),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/hhpred", "priority" -> 1.0),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/hmmer"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/patsearch"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/psiblast"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/alnviz"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/clustalo"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/kalign"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/mafft"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/msaprobs"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/muscle"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/tcoffee"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/aln2plot"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/hhpredid", "priority" -> 1.0),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/marcoil"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/pcoils", "priority" -> 1.0),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/repper"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/tprpred"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/ali2d"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/hhomp"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/quick2d", "priority" -> 1.0),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/modeller"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/samcc"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/ancescon"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/clans", "priority" -> 1.0),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/mmseqs2"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/phyml"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/sixframe"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/backtrans"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/formatseq"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/hhfilter"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/retseq"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/seq2id"),
      Map("url" -> "https://toolkit.tuebingen.mpg.de/#/tools/reformat")
    )
    pages.foreach { page =>
      val url: WebSitemapUrl = new WebSitemapUrl.Options(page apply "url" toString)
        .changeFreq(ChangeFreq.YEARLY)
        .priority((page getOrElse ("priority", 0.5)).asInstanceOf[Double])
        .build
      wsg.addUrl(url)
    }

    wsg.write
    Ok("Sitemap created!")
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
    userSessions.getUser.map { user =>
      // mainID exists, allow send File
      if (new java.io.File(
            s"${constants.jobPath}${constants.SEPARATOR}$mainID${constants.SEPARATOR}results${constants.SEPARATOR}$filename"
          ).exists)
        Ok.sendFile(
            new java.io.File(
              s"${constants.jobPath}${constants.SEPARATOR}$mainID${constants.SEPARATOR}results${constants.SEPARATOR}$filename"
            )
          )
          .withSession(userSessions.sessionCookie(request, user.sessionID.get))
          .as("text/plain") //TODO Only text/plain for files currently supported
      else
        Ok // TODO This needs more case validations

    }
  }

  def getStructureFile(filename: String): Action[AnyContent] = Action.async { implicit request =>
    {

      val db = Common.identifyDatabase(filename.replaceAll("(.cif)|(.pdb)", ""))
      val filepath = db match {
        case "scop" =>
          env.get("SCOPE")
        case "ecod" =>
          env.get("ECOD")
        case "mmcif" =>
          env.get("CIF")
      }
      Future.successful(Ok.sendFile(new java.io.File(s"$filepath${constants.SEPARATOR}$filename")).as("text/plain"))
    }
  }

  def javascriptRoutes: Action[AnyContent] = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        routes.javascript.Jobs.updateDateViewed,
        routes.javascript.Tool.frontendCount,
        routes.javascript.JobController.submitJob,
        routes.javascript.JobController.startJob,
        routes.javascript.JobController.checkHash,
        routes.javascript.JobController.delete,
        routes.javascript.JobController.loadJob,
        routes.javascript.DataController.get,
        routes.javascript.DataController.getHelp,
        routes.javascript.DataController.getRecentArticles,
        routes.javascript.Search.autoComplete,
        routes.javascript.Search.checkJobID,
        routes.javascript.Search.existsTool,
        routes.javascript.Search.get,
        routes.javascript.Search.getIndexPageInfo,
        routes.javascript.Search.getToolList,
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
        routes.javascript.HHompController.loadHits,
        routes.javascript.AlignmentController.loadHits,
        routes.javascript.AlignmentController.getAln,
        routes.javascript.AlignmentController.loadHitsClustal,
        routes.javascript.Application.ws
      )
    ).as("text/javascript").withHeaders(CACHE_CONTROL -> "max-age=31536000")
  }

  def matchSuperUserToPW(username: String, password: String): Future[Boolean] = {

    mongoStore.findUser(BSONDocument("userData.nameLogin" -> username)).map {

      case Some(user) if user.checkPassword(password) && user.isSuperuser => true
      case None                                                           => false

    }

  }

  def MaintenanceSecured[A]()(action: Action[A]): Action[A] = Action.async(action.parser) { request =>
    request.headers
      .get("Authorization")
      .flatMap { authorization =>
        authorization.split(" ").drop(1).headOption.filter { encoded =>
          new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
            case u :: p :: Nil if Await.result(matchSuperUserToPW(u, p), scala.concurrent.duration.Duration.Inf) =>
              true
            case _ => false
          }
        }
      }
      .map(_ => action(request))
      .getOrElse {
        Future.successful(Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured Area""""))
        //Future.successful(BadRequest())
      }
  }

  def maintenance: Action[AnyContent] = MaintenanceSecured() {

    Action { implicit ctx =>
      clusterMonitor ! Multicast

      Ok("Maintenance screen active...")

    }

  }

  val robots = Action { _ =>
    Ok(
      "User-agent: *\nAllow: /\nDisallow: /#/jobmanager/\nDisallow: /#/jobs/\nSitemap: https://toolkit.tuebingen.mpg.de/sitemap.xml"
    )
  }

}
