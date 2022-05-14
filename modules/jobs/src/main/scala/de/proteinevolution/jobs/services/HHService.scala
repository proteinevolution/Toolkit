/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

package de.proteinevolution.jobs.services

import cats.data.EitherT
import cats.implicits._
import de.proteinevolution.jobs.db.ResultFileAccessor
import de.proteinevolution.jobs.models.ResultsForm
import de.proteinevolution.jobs.results.{ HSP, SearchResult }
import io.circe.syntax._
import io.circe.{ DecodingFailure, Json, JsonObject }
import javax.inject.{ Inject, Singleton }
import play.api.Logging

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class HHService @Inject(
    toolFinder: ToolNameGetService,
    resultFiles: ResultFileAccessor
)(implicit ec: ExecutionContext)
    extends Logging {

  private def getResults(jobID: String): EitherT[Future, DecodingFailure, SearchResult[HSP]] = {
    EitherT((for {
      json <- resultFiles.getResults(jobID)
      tool <- toolFinder.getTool(jobID)
    } yield (json, tool)).map {
      case (json, tool) => resultFiles.parseResult(tool, json)
      case _ =>
        val error = "parsing result json failed."
        logger.error(error)
        Left(DecodingFailure(error, Nil))
    })
  }

  def loadHits(jobID: String, form: ResultsForm): EitherT[Future, DecodingFailure, Json] = {
    getResults(jobID).subflatMap { result =>
      var hits = if (form.sortBy.nonEmpty) {
        result.hitsOrderBy(form.sortBy.get, form.desc.getOrElse(false))
      } else result.HSPS
      if (form.filter.nonEmpty) {
        hits = hits.filter { hit =>
          (hit.description + hit.accession).toUpperCase.contains(form.filter.get.toUpperCase)
        }
      }
      val l = hits.length
      val s = Math.max(form.start.getOrElse(0), 0)
      val e = Math.min(form.end.getOrElse(l), l)
      Right(
        JsonObject(
          "total"         -> l.asJson,
          "totalNoFilter" -> result.HSPS.length.asJson,
          "start"         -> s.asJson,
          "end"           -> e.asJson,
          "hits"          -> hits.slice(s, e).map(_.toTableJson(result.db)).asJson
        ).asJson
      )
    }
  }

  def loadAlignments(jobID: String, start: Option[Int], end: Option[Int]): EitherT[Future, DecodingFailure, Json] = {
    getResults(jobID).subflatMap { result =>
      val l = result.HSPS.length
      val s = Math.max(start.getOrElse(0), 0)
      val e = Math.min(end.getOrElse(l), l)
      Right(
        JsonObject(
          "total"      -> l.asJson,
          "start"      -> s.asJson,
          "end"        -> e.asJson,
          "alignments" -> result.HSPS.slice(s, e).map(_.toAlignmentSectionJson(result.db)).asJson,
          "info"       -> result.toInfoJson
        ).asJson
      )
    }
  }

}
