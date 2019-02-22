/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.results.services

import cats.data.Reader
import de.proteinevolution.common.models.ToolName
import de.proteinevolution.common.models.ToolName._
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
