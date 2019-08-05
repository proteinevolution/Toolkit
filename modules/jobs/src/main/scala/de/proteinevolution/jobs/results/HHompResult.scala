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

case class HHompResult(
    HSPS: List[HHompHSP],
    num_hits: Int,
    query: SingleSeq,
    db: String,
    overall_prob: Double,
    alignment: AlignmentResult = AlignmentResult(Nil)
) extends SearchResult[HHompHSP] {

  override def hitsOrderBy(sortBy: String, desc: Boolean): List[HHompHSP] = {
    val l = sortBy match {
      case "acc"         => HSPS.sortBy(_.template.accession)
      case "name"        => HSPS.sortBy(_.description)
      case "probabHit"   => HSPS.sortBy(_.info.probab_hit)
      case "probabOMP"   => HSPS.sortBy(_.info.probab_OMP)
      case "eval"        => HSPS.sortBy(_.info.eval)
      case "ssScore"     => HSPS.sortBy(_.ss_score)
      case "alignedCols" => HSPS.sortBy(_.info.aligned_cols)
      case "templateRef" => HSPS.sortBy(_.template.ref)
      case _             => HSPS.sortBy(_.num)
    }
    if (desc) l.reverse else l
  }

  def toInfoJson: Json = {
    Map[String, Json](
      "probOMP" -> overall_prob.asJson,
      "query"   -> query.asJson
    ).asJson
  }

}

object HHompResult {

  implicit val hhompResultDecoder: Decoder[HHompResult] = (c: HCursor) =>
    for {
      hits        <- c.downField("results").downField("hits").as[List[Json]]
      alignments  <- c.downField("results").downField("alignments").as[List[Json]]
      db          <- c.downField("results").downField("db").as[String]
      query       <- c.downField("query").as[SingleSeq]
      overallProb <- c.downField("results").downField("overallprob").as[Double]
    } yield {
      val hspList = HHompHSP.hhompHSPListDecoder(hits, alignments)
      new HHompResult(hspList, hspList.length, query, db, overallProb)
    }

}
