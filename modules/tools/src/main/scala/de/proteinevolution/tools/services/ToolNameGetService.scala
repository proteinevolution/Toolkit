package de.proteinevolution.tools.services

import javax.inject.{ Inject, Singleton }

import de.proteinevolution.db.MongoStore
import de.proteinevolution.models.ToolNames.ToolName
import reactivemongo.bson.BSONDocument
import de.proteinevolution.models.database.jobs.Job
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
private[tools] class ToolNameGetService @Inject()(mongoStore: MongoStore)(implicit ec: ExecutionContext) {

  def getTool(jobID: String): Future[ToolName] = mongoStore.findJob(BSONDocument(Job.JOBID -> jobID)).map {
    case Some(x) => ToolName(x.tool)
    case None    => throw new IllegalArgumentException("job not found")
  }

}
