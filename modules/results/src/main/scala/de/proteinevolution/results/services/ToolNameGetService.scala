package de.proteinevolution.results.services

import de.proteinevolution.common.models.ToolName
import de.proteinevolution.jobs.dao.JobDao
import javax.inject.{ Inject, Singleton }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
private[results] class ToolNameGetService @Inject()(jobDao: JobDao)(implicit ec: ExecutionContext) {

  def getTool(jobID: String): Future[ToolName] = jobDao.findJob(jobID).map {
    case Some(x) => ToolName(x.tool)
    case None    => throw new IllegalArgumentException("job not found")
  }

}
