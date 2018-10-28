package de.proteinevolution.results.services

import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.models.ToolName
import javax.inject.{ Inject, Singleton }
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
private[results] class ToolNameGetService @Inject()(jobDao: JobDao)(implicit ec: ExecutionContext) {

  def getTool(jobID: String): Future[ToolName] = jobDao.findJob(BSONDocument(Job.JOBID -> jobID)).map {
    case Some(x) => ToolName(x.tool)
    case None    => throw new IllegalArgumentException("job not found")
  }

}
