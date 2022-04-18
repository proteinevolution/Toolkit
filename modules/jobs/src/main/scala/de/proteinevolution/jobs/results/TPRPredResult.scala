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

import de.proteinevolution.jobs.results.TPRPredResult._
import io.circe.{ Decoder, DecodingFailure, HCursor, Json }

case class TPRPredResult(info: List[Desc], hits: List[Hit])

object TPRPredResult {

  implicit def tprpredDecoder(jobId: String, json: Json): Either[DecodingFailure, TPRPredResult] =
    for {
      info <- json.hcursor.downField(jobId).downField("desc").as[List[Desc]]
      hits <- json.hcursor.downField(jobId).downField("hits").as[List[Hit]]
    } yield new TPRPredResult(info, hits)

  case class Desc(title: Option[String], value: Option[String])

  object Desc {
    implicit val descDecoder: Decoder[Desc] = (c: HCursor) =>
      for {
        title <- c.downArray.as[Option[String]]
        value <- c.downArray.right.as[Option[String]]
      } yield new Desc(title, value)
  }

  case class Hit(
      alignment: Option[String],
      repeat: Option[String],
      begin: Option[String],
      end: Option[String],
      pValue: Option[String]
  )

  object Hit {
    implicit val hitDecoder: Decoder[Hit] = (c: HCursor) =>
      for {
        alignment <- c.downArray.as[Option[String]]
        repeat    <- c.downN(1).as[Option[String]]
        begin     <- c.downN(2).as[Option[String]]
        end       <- c.downN(3).as[Option[String]]
        pValue    <- c.downN(4).as[Option[String]]
      } yield new Hit(alignment, repeat, begin, end, pValue)
  }

}
