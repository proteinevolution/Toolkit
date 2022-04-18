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

package de.proteinevolution.jobs.results

import de.proteinevolution.jobs.results.PatSearchResult.Hit
import io.circe._
import io.circe.generic.semiauto._

case class PatSearchResult(hits: List[Hit], len: Int)

object PatSearchResult {

  case class Hit(name: Option[String], seq: Option[String], pats: Option[List[Int]])

  object Hit {
    implicit val hitDecoder: Decoder[Hit] = deriveDecoder[Hit]
  }

  implicit def patSearchResultDecoder(json: Json, jobId: String): Either[DecodingFailure, PatSearchResult] =
    for {
      hitList <- json.hcursor.downField(jobId).downField("hits").as[List[Hit]]
      len     <- json.hcursor.downField(jobId).downField("len").as[Int]
    } yield new PatSearchResult(hitList, len)

}
