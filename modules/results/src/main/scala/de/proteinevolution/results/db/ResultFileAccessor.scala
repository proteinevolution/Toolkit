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

package de.proteinevolution.results.db

import better.files._
import de.proteinevolution.jobs.services.JobFolderValidation
import de.proteinevolution.common.models.ConstantsV2
import io.circe.Json
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

  def getResults(jobID: String): Future[Option[Json]] = {
    resultCache.get[Json](jobID).map {
      case Some(resultMap) =>
        Some(resultMap)
      case None =>
        if (resultsExist(jobID, constants)) {
          val files: List[File] =
            File(s"${constants.jobPath}/$jobID/results").list.withFilter(_.extension.contains(".json")).toList
          logger.info(s"Loading files for $jobID: ${files.map(_.name).mkString(",")}")
          val results: Json =
            files
              .map { file =>
                file.nameWithoutExtension -> parse(file.contentAsString).right.toOption.getOrElse {
                  logger.error("Invalid result json from database")
                  throw new NoSuchElementException
                }
              }
              .toMap[String, Json]
              .updated("jobID", jobID.asJson)
              .asJson
          resultCache.set(jobID, results, 10.minutes)
          Some(results)
        } else {
          None
        }
    }
  }

}
