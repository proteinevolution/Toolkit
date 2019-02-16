package de.proteinevolution.jobs.services
import java.time.ZonedDateTime

import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.common.models.database.jobs.JobState.Done
import de.proteinevolution.common.models.database.statistics.{ JobEvent, JobEventLog }
import javax.inject.{ Inject, Singleton }
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class JobFrontendToolsService @Inject()(
    jobIdProvider: JobIdProvider,
    jobDao: JobDao
)(implicit ec: ExecutionContext) {

  def logFrontendJob(toolName: String): Future[WriteResult] = {
    for {
      jobId <- jobIdProvider.provide
      log = generateJobEventLog(jobId, toolName)
      wr <- jobDao.addJobLog(log)
    } yield wr
  }

  private def generateJobEventLog(jobId: String, toolName: String): JobEventLog = {
    JobEventLog(
      jobID = jobId,
      toolName = toolName.trim.toLowerCase,
      events = JobEvent(Done, Some(ZonedDateTime.now)) :: Nil
    )
  }

}
