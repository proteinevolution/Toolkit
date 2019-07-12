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

import de.proteinevolution.jobs.results.General.{ DTParam, SingleSeq }
import io.circe.{ Decoder, HCursor }

case class HmmerResult(
    HSPS: List[HmmerHSP],
    num_hits: Int,
    alignment: AlignmentResult,
    query: SingleSeq,
    db: String,
    TMPRED: String,
    COILPRED: String
) extends SearchResult[HmmerHSP] {
  def hitsOrderBy(params: DTParam): List[HmmerHSP] = {
    (params.orderCol, params.orderDir) match {
      case (1, "asc")  => HSPS.sortBy(_.accession)
      case (1, "desc") => HSPS.sortWith(_.accession > _.accession)
      case (2, "asc")  => HSPS.sortBy(_.description)
      case (2, "desc") => HSPS.sortWith(_.description > _.description)
      case (3, "asc")  => HSPS.sortBy(_.full_evalue)
      case (3, "desc") => HSPS.sortWith(_.full_evalue > _.full_evalue)
      case (4, "asc")  => HSPS.sortBy(_.eValue)
      case (4, "desc") => HSPS.sortWith(_.eValue > _.eValue)
      case (5, "asc")  => HSPS.sortBy(_.bitscore)
      case (5, "desc") => HSPS.sortWith(_.bitscore > _.bitscore)
      case (6, "asc")  => HSPS.sortBy(_.hit_len)
      case (6, "desc") => HSPS.sortWith(_.hit_len > _.hit_len)
      case (_, "asc")  => HSPS.sortBy(_.num)
      case (_, "desc") => HSPS.sortWith(_.num > _.num)
      case (_, _)      => HSPS.sortBy(_.num)
    }
  }
}

object HmmerResult {

  implicit val hmmerResultDecoder: Decoder[HmmerResult] = (c: HCursor) =>
    for {
      jobId           <- c.downField("jobID").as[String]
      alignmentResult <- c.downField("alignment").as[Option[AlignmentResult]]
      hsps            <- c.downField(jobId).downField("hsps").as[List[HmmerHSP]]
      db              <- c.downField(jobId).downField("db").as[Option[String]]
      query           <- c.downField("query").as[SingleSeq]
      tmpred          <- c.downField(jobId).downField("TMPRED").as[Option[String]]
      coilpred        <- c.downField(jobId).downField("COILPRED").as[Option[String]]
    } yield
      new HmmerResult(
        hsps,
        hsps.length,
        alignmentResult.getOrElse(AlignmentResult(Nil)),
        query,
        db.getOrElse(""),
        tmpred.getOrElse("0"),
        coilpred.getOrElse("1")
    )

}
