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

package de.proteinevolution.results.results

import de.proteinevolution.results.results.General.{ DTParam, SingleSeq }
import io.circe._

case class PSIBlastResult(
    HSPS: List[PSIBlastHSP],
    num_hits: Int,
    iter_num: Int,
    db: String,
    eValue: Double,
    query: SingleSeq,
    belowEvalThreshold: Int,
    TMPRED: String,
    COILPRED: String,
    alignment: AlignmentResult = AlignmentResult(Nil)
) extends SearchResult[PSIBlastHSP] {

  override def hitsOrderBy(params: DTParam): List[PSIBlastHSP] = {
    (params.orderCol, params.orderDir) match {
      case (1, "asc")  => HSPS.sortBy(_.accession)
      case (1, "desc") => HSPS.sortWith(_.accession > _.accession)
      case (2, "asc")  => HSPS.sortBy(_.description)
      case (2, "desc") => HSPS.sortWith(_.description > _.description)
      case (3, "asc")  => HSPS.sortBy(_.eValue)
      case (3, "desc") => HSPS.sortWith(_.eValue > _.eValue)
      case (4, "asc")  => HSPS.sortBy(_.bitScore)
      case (4, "desc") => HSPS.sortWith(_.bitScore > _.bitScore)
      case (5, "asc")  => HSPS.sortBy(_.ref_len)
      case (5, "desc") => HSPS.sortWith(_.ref_len > _.ref_len)
      case (6, "asc")  => HSPS.sortBy(_.hit_len)
      case (6, "desc") => HSPS.sortWith(_.hit_len > _.hit_len)
      case (_, "asc")  => HSPS.sortBy(_.num)
      case (_, "desc") => HSPS.sortWith(_.num > _.num)
      case (_, _)      => HSPS.sortBy(_.num)
    }
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
      tmpred    <- c.downField("output_psiblastp").downField("TMPRED").as[Option[String]]
      coilpred  <- c.downField("output_psiblastp").downField("COILPRED").as[Option[String]]
    } yield {
      val num_hits   = hits.length
      val hspList    = hits.flatMap(_.hcursor.as[PSIBlastHSP](PSIBlastHSP.parseHSP(db)).toOption)
      val upperBound = calculateUpperBound(hits, eValue).getOrElse(hspList.length + 1)
      new PSIBlastResult(
        hspList,
        num_hits,
        iter_list.size - 1,
        db,
        eValue.toDouble,
        query,
        upperBound,
        tmpred.getOrElse("0"),
        coilpred.getOrElse("1")
      )
    }
  }

  private[this] def calculateUpperBound(hits: List[Json], eValue: String): Option[Int] = {
    // take the smallest value above the threshold
    (for {
      hit <- hits
      cursor = hit.hcursor
      eval <- cursor.downField("hsps").downArray.first.downField("evalue").as[Double].toOption
      num  <- cursor.downField("num").as[Int].toOption
      if eval >= eValue.toDouble
    } yield (eval, num)).sortWith(_._1 < _._1).toMap.headOption.map(_._2)
  }

}
