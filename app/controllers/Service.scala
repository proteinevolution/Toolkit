package controllers

import java.io.{ FileInputStream, ObjectInputStream }
import javax.inject.{ Inject, Singleton }

import akka.util.Timeout
import models.UserSessions
import de.proteinevolution.models.database.jobs.{ Done, JobState }
import play.api.Logger
import play.api.cache._
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.mvc._
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoComponents }
import better.files._
import models.tools.{ Param, ToolFactory }
import de.proteinevolution.db.MongoStore
import java.time.format.DateTimeFormatter

import de.proteinevolution.common.LocationProvider
import de.proteinevolution.models.Constants
import models.tools.ToolFactory.{ Jobitem, Toolitem }
import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.twirl.api.Html

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import org.webjars.play.WebJarsUtil

@Singleton
final class Service @Inject()(webJarsUtil: WebJarsUtil, // TODO not used
                              messagesApi: MessagesApi,
                              val reactiveMongoApi: ReactiveMongoApi,
                              mongoStore: MongoStore,
                              userSessions: UserSessions,
                              @NamedCache("userCache") implicit val userCache: SyncCacheApi,
                              implicit val locationProvider: LocationProvider,
                              toolFactory: ToolFactory,
                              constants: Constants,
                              cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with I18nSupport
    with ReactiveMongoComponents {

  implicit val timeout: Timeout = Timeout(1.seconds)

  def static(static: String): Action[AnyContent] = Action { implicit request =>
    static match {

      case "sitemap" =>
        Ok(views.html.general.sitemap())

      // Frontend tools
      case "reformat" =>
        Ok(views.html.tools.forms.reformat("Utils"))

      case _ =>
        Ok(views.html.errors.pagenotfound()) //Bug: Mithril only accepts 200 to re-route

    }
  }

  implicit def htmlWrites: Writes[Html] = new Writes[Html] {

    def writes(html: Html) = JsString(html.body)
  }

  implicit val toolitemWrites: Writes[Toolitem] = (
    (JsPath \ "toolname").write[String] and
    (JsPath \ "toolnameLong").write[String] and
    (JsPath \ "toolnameAbbrev").write[String] and
    (JsPath \ "category").write[String] and
    (JsPath \ "optional").write[String] and
    (JsPath \ "params").write[Seq[(String, Seq[Param])]]
  )(unlift(Toolitem.unapply))

  implicit val jobitemWrites: Writes[Jobitem] = (
    (JsPath \ "jobID").write[String] and
    (JsPath \ "state").write[JobState] and
    (JsPath \ "dateCreated").write[String] and
    (JsPath \ "toolitem").write[Toolitem] and
    (JsPath \ "views").write[Seq[String]] and
    (JsPath \ "paramValues").write[Map[String, String]](play.api.libs.json.Writes.mapWrites[String])
  )(unlift(Jobitem.unapply))

  def getTool(toolname: String) = Action {
    toolFactory.values.get(toolname) match {
      case Some(tool) => Ok(Json.toJson(tool.toolitem))
      case None       => NotFound
    }
  }

  // Fetches the result of a job for a particular result panel
  def getResult(jobID: String, tool: String, resultpanel: String): Action[AnyContent] = Action.async {
    implicit request =>
      val resultPanel = toolFactory.resultMap(tool)(resultpanel)(jobID, request)
      resultPanel.map(Ok(_))
  }

  // TODO Change that not all jobViews but only the resultpanel titles are returned
  def getJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    mongoStore.selectJob(jobID).flatMap {
      case Some(job) =>
        Logger.info("Requested job has been found in MongoDB, the jobState is " + job.status)
        val toolitem = toolFactory.values(job.tool).toolitem

        // The jobState decides which views will be appended to the job
        val jobViews: Future[Seq[String]] = job.status match {
          case Done => Future.successful(toolFactory.resultPanels(toolitem.toolname))
          // All other views are currently computed on Clientside
          case _ => Future.successful(Seq.empty[String])
        }
        // Read parameters from serialized file
        val paramValues: Map[String, String] = {
          if ((constants.jobPath / jobID / "sparam").exists) {
            val ois =
              new ObjectInputStream(new FileInputStream((constants.jobPath / jobID / "sparam").pathAsString))
            val x = ois.readObject().asInstanceOf[Map[String, String]]
            ois.close()
            x
          } else {
            Map.empty[String, String]
          }
        }

        jobViews.map { jobViewsN =>
          Ok(
            Json.toJson(
              Jobitem(
                job.jobID,
                job.status,
                job.dateCreated.get.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                toolitem,
                jobViewsN,
                paramValues
              )
            )
          )

        }
      case _ =>
        Logger.info("Job could not be found")
        Future.successful(NotFound)
    }
  }
}
