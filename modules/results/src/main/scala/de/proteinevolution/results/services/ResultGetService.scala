package de.proteinevolution.results.services

import java.time.ZonedDateTime

import better.files._
import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.base.helpers.ToolkitTypes
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.jobs.services.JobFolderValidation
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.JobState.{ Done, Pending, Prepared }
import de.proteinevolution.models.forms.JobForm
import de.proteinevolution.tools.ToolConfig
import javax.inject.{ Inject, Singleton }
import play.api.Logger
import play.api.cache.{ AsyncCacheApi, NamedCache }
import play.twirl.api.HtmlFormat
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ResultGetService @Inject()(
    resultViewFactory: ResultViewFactory,
    jobDao: JobDao,
    toolConfig: ToolConfig,
    constants: ConstantsV2,
    @NamedCache("resultCache") resultCache: AsyncCacheApi
)(implicit ec: ExecutionContext)
    extends JobFolderValidation
    with ToolkitTypes {

  private val logger = Logger(this.getClass)

  def get(
      jobId: String,
      tool: String,
      resultView: String
  ): OptionT[Future, HtmlFormat.Appendable] = {
    resultViewFactory
      .apply(tool, jobId)
      .map(_.tabs(resultView))
      .toRight[Unit](())
      .leftMap { _ =>
        logger.error(s"result for $jobId could not be found.")
        cleanLostJobs(jobId)
      }
      .toOption
  }

  def getJob(jobId: String): OptionT[Future, JobForm] = {
    val paramValues: Map[String, String] = {
      if (paramsExist(jobId, constants)) {
        (constants.jobPath / jobId / "sparam").readDeserialized[Map[String, String]]
      } else {
        Map.empty[String, String]
      }
    }
    (for {
      job      <- OptionT(jobDao.selectJob(jobId))
      toolName <- OptionT.pure[Future](toolConfig.values(job.tool).toolFormSimple.name)
      jobViews <- OptionT.liftF(jobViews(job, toolName))
    } yield {
      (job, jobViews)
    }).map {
      case (job, jobViews) =>
        JobForm(
          job.jobID,
          job.status,
          job.tool,
          toolConfig.values(job.tool).code,
          job.dateCreated.getOrElse(ZonedDateTime.now).toInstant.toEpochMilli,
          jobViews,
          paramValues
        )
    }
  }

  private def jobViews(job: Job, toolName: String): Future[Seq[String]] = job.status match {
    case Done =>
      resultViewFactory(toolName, job.jobID).value.map {
        case Some(r) => r.tabs.keys.toSeq
        case None =>
          logger.error(s"no views found for $job")
          Nil
      }
    case _ => fuccess(Nil)
  }

  private def cleanLostJobs(jobId: String): OptionT[Future, Unit] = {
    for {
      job <- OptionT(jobDao.findJob(BSONDocument(Job.JOBID -> jobId)))
      jobs <- OptionT.liftF(
        jobDao
          .findJobs(BSONDocument(Job.HASH -> job.hash))
          .map(_.filter(x => (Prepared :: Pending :: Nil).contains(x.status)))
      )
    } yield {
      jobs.foreach { job =>
        jobDao.removeJob(BSONDocument(Job.JOBID -> job.jobID))
        resultCache.remove(job.jobID)
      }
    }
  }

}
