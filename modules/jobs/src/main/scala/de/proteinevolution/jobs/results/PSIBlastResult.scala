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
import io.circe._
import io.circe.syntax._

case class PSIBlastResult(
    HSPS: List[PSIBlastHSP],
    num_hits: Int,
    iter_num: Int,
    db: String,
    eValue: Double,
    query: SingleSeq,
    belowEvalThreshold: Int,
    tmpred: String,
    coilpred: String,
    signal: String,
    alignment: AlignmentResult = AlignmentResult(Nil)
) extends SearchResult[PSIBlastHSP] {

  def hitsOrderBy(sortBy: String, desc: Boolean): List[PSIBlastHSP] = {
    val l = sortBy match {
      case "acc"      => HSPS.sortBy(_.accession)
      case "name"     => HSPS.sortBy(_.description)
      case "eval"     => HSPS.sortBy(_.eValue)
      case "bitScore" => HSPS.sortBy(_.bitScore)
      case "refLen"   => HSPS.sortBy(_.ref_len)
      case "hitLen"   => HSPS.sortBy(_.hit_len)
      case _          => HSPS.sortBy(_.num)
    }
    if (desc) l.reverse else l
  }

  def toInfoJson: Json = {
    Map[String, Json](
      "num_hits"           -> num_hits.asJson,
      "tm"                 -> tmpred.asJson,
      "coil"               -> coilpred.asJson,
      "signal"             -> signal.asJson,
      "query"              -> query.asJson,
      "belowEvalThreshold" -> belowEvalThreshold.asJson
    ).asJson
  }

}

object PSIBlastResult {

  implicit val decodePsiBlastResult: Decoder[PSIBlastResult] = (c: HCursor) => {
    val iterations = c
      .downField("output_psiblastp")
      .downField("BlastOutput2")
      .downArray
      .first
      .downField("report")
      .downField("results")
      .downField("iterations")
    for {
      query     <- c.downField("query").as[SingleSeq]
      iter_list <- iterations.as[List[Json]]
      db        <- c.downField("output_psiblastp").downField("db").as[String]
      eValue    <- c.downField("output_psiblastp").downField("evalue").as[String]
      hits      <- iterations.downArray.rightN(iter_list.size - 1).downField("search").downField("hits").as[List[Json]]
      tmpred    <- c.downField("output_psiblastp").downField("tmpred").as[Option[String]]
      coilpred  <- c.downField("output_psiblastp").downField("coilpred").as[Option[String]]
      signal    <- c.downField("output_psiblastp").downField("signal").as[Option[String]]
    } yield {
      val num_hits   = hits.length
      val hspList    = hits.flatMap(_.hcursor.as[PSIBlastHSP](PSIBlastHSP.parseHSP(db)).toOption)
      val upperBound = calculateUpperBound(hits, eValue)
      new PSIBlastResult(
        hspList,
        num_hits,
        iter_list.size - 1,
        db,
        eValue.toDouble,
        query,
        upperBound,
        tmpred.getOrElse("0"),
        coilpred.getOrElse("1"),
        signal.getOrElse("0")
      )
    }
  }

  private[this] def calculateUpperBound(hits: List[Json], eValue: String): Int = {
    // count the number of times eValue >= eval of hsps
    (for {
      hit <- hits
      cursor = hit.hcursor
      eval <- cursor.downField("hsps").downArray.first.downField("evalue").as[Double].toOption
      if eval <= eValue.toDouble
    } yield eval).length
  }

}
