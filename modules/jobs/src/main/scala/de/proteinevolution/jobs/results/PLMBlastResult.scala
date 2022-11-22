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

import de.proteinevolution.jobs.results.General.SingleSeq
import io.circe.syntax._
import io.circe.{ Decoder, HCursor, Json }

case class PLMBlastResult(
    HSPS: List[PLMBlastHSP],
    num_hits: Int,
    alignment: AlignmentResult,
    query: SingleSeq,
    db: String
) extends SearchResult[PLMBlastHSP] {

  def hitsOrderBy(sortBy: String, desc: Boolean): List[PLMBlastHSP] = {
    val l = sortBy match {
      case "acc"      => HSPS.sortBy(_.accession)
      case "name"     => HSPS.sortBy(_.description)
      case "eval"     => HSPS.sortBy(_.eValue)
      case "bitScore" => HSPS.sortBy(_.simScore)
      case "hitLen"   => HSPS.sortBy(_.hit_len)
      case _          => HSPS.sortBy(_.num)
    }
    if (desc) l.reverse else l
  }

  def toInfoJson: Json = {
    Map[String, Json](
      "num_hits" -> num_hits.asJson,
      "query"    -> query.asJson
    ).asJson
  }

}

object PLMBlastResult {

  println("here==============================================================")

  implicit val plmblastResultDecoder: Decoder[PLMBlastResult] = (c: HCursor) =>
    for {
      alignmentResult <- c.downField("alignment").as[Option[AlignmentResult]]
      hsps            <- c.downField("results").downField("hsps").as[List[PLMBlastHSP]]
      db              <- c.downField("results").downField("db").as[Option[String]]
      query           <- c.downField("query").as[SingleSeq]
    } yield new PLMBlastResult(
      hsps,
      hsps.length,
      alignmentResult.getOrElse(AlignmentResult(Nil)),
      query,
      db.getOrElse("")
    )

}
