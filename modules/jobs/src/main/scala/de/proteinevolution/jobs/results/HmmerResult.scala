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

package de.proteinevolution.jobs.results

import de.proteinevolution.jobs.results.General.SingleSeq
import io.circe.syntax._
import io.circe.{Decoder, HCursor, Json}

case class HmmerResult(
    HSPS: List[HmmerHSP],
    num_hits: Int,
    alignment: AlignmentResult,
    query: SingleSeq,
    db: String,
    tmpred: String,
    coilpred: String,
    signal: String
) extends SearchResult[HmmerHSP] {

  def hitsOrderBy(sortBy: String, desc: Boolean): List[HmmerHSP] = {
    val l = sortBy match {
      case "acc"      => HSPS.sortBy(_.accession)
      case "name"     => HSPS.sortBy(_.description)
      case "fullEval" => HSPS.sortBy(_.full_evalue)
      case "eval"     => HSPS.sortBy(_.eValue)
      case "bitScore" => HSPS.sortBy(_.bitScore)
      case "hitLen"   => HSPS.sortBy(_.hit_len)
      case _          => HSPS.sortBy(_.num)
    }
    if (desc) l.reverse else l
  }

  def toInfoJson: Json = {
    Map[String, Json](
      "num_hits" -> num_hits.asJson,
      "tm"       -> tmpred.asJson,
      "coil"     -> coilpred.asJson,
      "signal"   -> signal.asJson,
      "query"    -> query.asJson
    ).asJson
  }

}

object HmmerResult {

  implicit val hmmerResultDecoder: Decoder[HmmerResult] = (c: HCursor) =>
    for {
      alignmentResult <- c.downField("alignment").as[Option[AlignmentResult]]
      hsps            <- c.downField("results").downField("hsps").as[List[HmmerHSP]]
      db              <- c.downField("results").downField("db").as[Option[String]]
      query           <- c.downField("query").as[SingleSeq]
      tmpred          <- c.downField("results").downField("tmpred").as[Option[String]]
      coilpred        <- c.downField("results").downField("coilpred").as[Option[String]]
      signal          <- c.downField("results").downField("signal").as[Option[String]]
    } yield new HmmerResult(
      hsps,
      hsps.length,
      alignmentResult.getOrElse(AlignmentResult(Nil)),
      query,
      db.getOrElse(""),
      tmpred.getOrElse("0"),
      coilpred.getOrElse("1"),
      signal.getOrElse("0")
    )

}
