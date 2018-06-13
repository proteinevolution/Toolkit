package controllers

import java.io.{ FileInputStream, ObjectInputStream }
import javax.inject.{ Inject, Singleton }

import akka.util.Timeout
import de.proteinevolution.models.database.jobs.JobState._
import play.api.Logger
import play.api.cache._
import play.api.i18n.I18nSupport
import play.api.mvc._
import better.files._
import models.tools.ToolFactory
import de.proteinevolution.db.MongoStore
import java.time.format.DateTimeFormatter
import de.proteinevolution.common.LocationProvider
import de.proteinevolution.models.forms.JobForm
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.Job
import play.api.libs.json._
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._

@Singleton
final class Service @Inject()(
    mongoStore: MongoStore,
    toolFactory: ToolFactory,
    constants: ConstantsV2,
    cc: ControllerComponents,
    assets: AssetsFinder
)(implicit ec: ExecutionContext,
  @NamedCache("resultCache") val resultCache: AsyncCacheApi,
  @NamedCache("userCache") val userCache: SyncCacheApi,
  val locationProvider: LocationProvider)
    extends AbstractController(cc)
    with I18nSupport {

  implicit val timeout: Timeout = Timeout(1.seconds)

  private val logger = Logger(this.getClass)

  def static(static: String): Action[AnyContent] = Action { implicit request =>
    static match {
      // Frontend tools
      case "reformat" =>
        Ok(views.html.tools.forms.reformat(assets))
      case _ =>
        NotFound(views.html.errors.pagenotfound(assets))
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
      innerMap(resultpanel)(jobID).map { html =>
        if (html == views.html.errors.resultnotfound()) {
          mongoStore
            .findJob(BSONDocument(Job.JOBID -> jobID))
            .map(_.map { job =>
              mongoStore
                .findJobs(BSONDocument(Job.HASH -> job.hash))
                .map(_.filter(x => (Prepared :: Pending :: Nil).contains(x.status)).map { job =>
                  logger.info(s"delete all prepared jobs with same hash value as $jobID from database and cache")
                  // so that users don't see them in their joblist and try to reload them
                  mongoStore.removeJob(BSONDocument(Job.JOBID -> job.jobID))
                  resultCache.remove(job.jobID)
                })
            })
          mongoStore.removeJob(BSONDocument(Job.JOBID -> jobID))
          resultCache.remove(jobID)
          logger.info(s"deleted $jobID from the database and cache because the job result could not be loaded.")
        }
        Ok(JsString(html.body))
      }
  }

  def getJob(jobID: String): Action[AnyContent] = Action.async { implicit request =>
    mongoStore.selectJob(jobID).flatMap {
      case Some(job) =>
        logger.info("Requested job has been found in MongoDB, the jobState is " + job.status)
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
        logger.info("Job could not be found")
        Future.successful(NotFound)
    }
  }
}
