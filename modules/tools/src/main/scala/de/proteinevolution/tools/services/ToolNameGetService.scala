package de.proteinevolution.tools.services

import javax.inject.{ Inject, Singleton }

import de.proteinevolution.db.MongoStore
import de.proteinevolution.models.ToolNames.ToolName

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
private[tools] class ToolNameGetService @Inject()(mongoStore: MongoStore)(implicit ec: ExecutionContext) {

  def getTool(jobID: String): Future[ToolName] = mongoStore.selectJob(jobID).map {
    case Some(x) => ToolName(x.tool)
    case None    => throw new IllegalArgumentException("job not found")
  }

}
