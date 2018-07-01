package de.proteinevolution.results.services

import java.io.{ FileInputStream, ObjectInputStream }
import java.time.format.DateTimeFormatter

import better.files._
import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.models.database.jobs.JobState.{ Done, Pending, Prepared }
import de.proteinevolution.models.forms.JobForm
import de.proteinevolution.services.ToolConfig
import javax.inject.{ Inject, Singleton }
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
)(implicit ec: ExecutionContext) {

  def get(jobId: String, tool: String, resultView: String): Future[HtmlFormat.Appendable] = {
    resultViewFactory.apply(tool, jobId).value.map {
      case Some(view) => view.tabs(resultView)
      case None =>
        cleanLostJobs(jobId)
        views.html.errors.resultnotfound()
    }
  }

  def getJob(jobId: String): OptionT[Future, JobForm] = {
    (for {
      job <- OptionT(jobDao.selectJob(jobId))
    } yield {
      val toolForm = toolConfig.values(job.tool).toolForm
      val jobViews: Future[Seq[String]] = job.status match {
        case Done =>
          resultViewFactory(toolForm.toolname, jobId).value.map {
            case Some(r) => r.tabs.keys.toSeq
            case None    => Nil // TODO throw some exception or something
          }
        case _ => Future.successful(Nil)
      }
      val paramValues: Map[String, String] = {
        if ((constants.jobPath / jobId / "sparam").exists) {
          val ois =
            new ObjectInputStream(new FileInputStream((constants.jobPath / jobId / "sparam").pathAsString))
          val x = ois.readObject().asInstanceOf[Map[String, String]]
          ois.close()
          x
        } else {
          Map.empty[String, String]
        }
      }
      jobViews.map { jobViewsN =>
        JobForm(
          job.jobID,
          job.status,
          job.dateCreated.get.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
          toolForm,
          jobViewsN,
          paramValues
        )
      }
    }).flatMap(OptionT.liftF(_))
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
