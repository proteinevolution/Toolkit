package de.proteinevolution.results.services

import cats.data.Reader
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.models.ToolName
import de.proteinevolution.results.services.ResultsRepository.ResultsService
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait ResultsRepository {

  def getResults(jobId: String): Reader[ResultsService, Future[Option[JsValue]]] =
    Reader[ResultsService, Future[Option[JsValue]]] { rs =>
      rs.resultFiles.getResults(jobId)
    }

  def getTool(jobId: String): Reader[ResultsService, Future[ToolName]] = Reader[ResultsService, Future[ToolName]] {
    rs =>
      rs.toolFinder.getTool(jobId)
  }

}

object ResultsRepository {

  case class ResultsService(toolFinder: ToolNameGetService, resultFiles: ResultFileAccessor)

}
