package de.proteinevolution.results.services

import cats.data.Reader
import de.proteinevolution.models.ToolName
import de.proteinevolution.models.ToolName._
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.results.results._
import de.proteinevolution.results.services.ResultsRepository.ResultsService
import io.circe.{ DecodingFailure, Json }

import scala.concurrent.Future

trait ResultsRepository {

  protected def getResults(jobId: String): Reader[ResultsService, Future[Option[Json]]] =
    Reader[ResultsService, Future[Option[Json]]] { rs =>
      rs.resultFiles.getResults(jobId)
    }

  protected def getTool(jobId: String): Reader[ResultsService, Future[ToolName]] =
    Reader[ResultsService, Future[ToolName]] { rs =>
      rs.toolFinder.getTool(jobId)
    }

  protected def parseResult(
      tool: ToolName,
      json: Json
  ): Either[DecodingFailure, (SearchResult[HSP], ToolName)] = {
    (tool match {
      case HHBLITS  => json.as[HHBlitsResult]
      case HHPRED   => json.as[HHPredResult]
      case HHOMP    => json.as[HHompResult]
      case HMMER    => json.as[HmmerResult]
      case PSIBLAST => json.as[PSIBlastResult]
      case _        => throw new IllegalArgumentException("tool has no hitlist")
    }).map((_, tool))
  }

}

object ResultsRepository {

  case class ResultsService(toolFinder: ToolNameGetService, resultFiles: ResultFileAccessor)

}
