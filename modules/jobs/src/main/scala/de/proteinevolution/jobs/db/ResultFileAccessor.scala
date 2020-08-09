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

package de.proteinevolution.jobs.db

import java.io.FileNotFoundException

import better.files._
import de.proteinevolution.common.models.{ ConstantsV2, ToolName }
import de.proteinevolution.common.models.ToolName.{ HHBLITS, HHOMP, HHPRED, HMMER, PSIBLAST }
import de.proteinevolution.jobs.results.{
  HHBlitsResult,
  HHPredResult,
  HHompResult,
  HSP,
  HmmerResult,
  PSIBlastResult,
  SearchResult
}
import de.proteinevolution.jobs.services.JobFolderValidation
import io.circe.{ DecodingFailure, Json }
import io.circe.parser._
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.Logging
import play.api.cache.{ AsyncCacheApi, NamedCache }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class ResultFileAccessor @Inject()(
    constants: ConstantsV2,
    @NamedCache("resultCache") resultCache: AsyncCacheApi
)(implicit ec: ExecutionContext)
    extends JobFolderValidation
    with Logging {

  def getResults(jobID: String): Future[Json] = {
    if (resultsExist(jobID, constants)) {
      resultCache.getOrElseUpdate[Json](jobID, 10.minutes) {
        val files: List[File] =
          File(s"${constants.jobPath}/$jobID/results").list.withFilter(_.extension.contains(".json")).toList
        logger.info(s"Loading files for $jobID: ${files.map(_.name).mkString(",")}")
        Future {
          files
            .map { file =>
              file.nameWithoutExtension -> parse(file.contentAsString).toOption.getOrElse {
                logger.error("Invalid result json")
                throw new NoSuchElementException
              }
            }
            .toMap[String, Json]
            .updated("jobID", jobID.asJson)
            .asJson
        }
      }
    } else {
      throw new FileNotFoundException(s"result file for $jobID not found")
    }
  }

  def parseResult(
      tool: ToolName,
      json: Json
  ): Either[DecodingFailure, SearchResult[HSP]] = {
    tool match {
      case HHBLITS  => json.as[HHBlitsResult]
      case HHPRED   => json.as[HHPredResult]
      case HHOMP    => json.as[HHompResult]
      case HMMER    => json.as[HmmerResult]
      case PSIBLAST => json.as[PSIBlastResult]
      case _        => throw new IllegalArgumentException("tool has no hitlist")
    }
  }

}
