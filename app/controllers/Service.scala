package controllers

import java.io.{ FileInputStream, ObjectInputStream }
import javax.inject.{ Inject, Singleton }

import akka.util.Timeout
import de.proteinevolution.models.database.jobs.Done
import play.api.Logger
import play.api.cache._
import play.api.i18n.I18nSupport
import play.api.mvc._
import play.modules.reactivemongo.{ ReactiveMongoApi, ReactiveMongoComponents }
import better.files._
import models.tools.ToolFactory
import de.proteinevolution.db.MongoStore
import java.time.format.DateTimeFormatter

import de.proteinevolution.common.LocationProvider
import de.proteinevolution.models.forms.JobForm
import de.proteinevolution.models.Constants
import play.api.libs.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

@Singleton
final class Service @Inject()(val reactiveMongoApi: ReactiveMongoApi,
                              mongoStore: MongoStore,
                              toolFactory: ToolFactory,
                              constants: Constants,
                              cc: ControllerComponents)(implicit ec: ExecutionContext,
                                                        val locationProvider: LocationProvider,
                                                        @NamedCache("userCache") val userCache: SyncCacheApi)
    extends AbstractController(cc)
    with I18nSupport
    with ReactiveMongoComponents {

  implicit val timeout: Timeout = Timeout(1.seconds)

  def static(static: String): Action[AnyContent] = Action { implicit request =>
    static match {
      // Frontend tools
      case "reformat" =>
        Ok(views.html.tools.forms.reformat())
      case _ =>
        NotFound(views.html.errors.pagenotfound())
    }
  }

  def getTool(toolname: String) = Action {
    toolFactory.values.get(toolname) match {
      case Some(tool) => Ok(Json.toJson(tool.toolForm))
      case None       => NotFound
    }
  }

  def getResult(jobID: String, tool: String, resultpanel: String): Action[AnyContent] = Action.async {
    implicit request =>
      val innerMap = toolFactory.getResultMap(tool)
      innerMap(resultpanel)(jobID).map(html => Ok(JsString(html.body)))
  }

  def getJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    mongoStore.selectJob(jobID).flatMap {
      case Some(job) =>
        Logger.info("Requested job has been found in MongoDB, the jobState is " + job.status)
        val toolForm = toolFactory.values(job.tool).toolForm
        // The jobState decides which views will be appended to the job
        val jobViews: Future[Seq[String]] = job.status match {
          case Done =>
            Future.successful(toolFactory.getResultMap(toolForm.toolname).keys.toSeq)
          // All other views are currently computed on Clientside
          case _ => Future.successful(Nil)
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
              JobForm(
                job.jobID,
                job.status,
                job.dateCreated.get.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                toolForm,
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
