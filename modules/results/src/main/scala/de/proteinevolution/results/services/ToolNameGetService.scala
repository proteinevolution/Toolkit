package de.proteinevolution.results.services

import de.proteinevolution.db.MongoStore
import de.proteinevolution.models.ToolName
import de.proteinevolution.models.database.jobs.Job
import javax.inject.{ Inject, Singleton }
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
private[results] class ToolNameGetService @Inject()(mongoStore: MongoStore)(implicit ec: ExecutionContext) {

  def getTool(jobID: String): Future[ToolName] = mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).map {
    case Some(x) => ToolName(x.tool)
    case None    => throw new IllegalArgumentException("job not found")
  }

}
