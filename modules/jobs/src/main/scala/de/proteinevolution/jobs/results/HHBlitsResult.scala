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
import io.circe.{ Decoder, HCursor, Json }

case class HHBlitsResult(
    HSPS: List[HHBlitsHSP],
    alignment: AlignmentResult,
    num_hits: Int,
    query: SingleSeq,
    db: String,
    TMPRED: String,
    COILPRED: String
) extends SearchResult[HHBlitsHSP] {

  override def hitsOrderBy(sortBy: String, desc: Boolean): List[HHBlitsHSP] = {
    val l = sortBy match {
      case "acc"         => HSPS.sortBy(_.template.accession)
      case "name"        => HSPS.sortBy(_.description)
      case "probab"   => HSPS.sortBy(_.info.probab)
      case "eval"        => HSPS.sortBy(_.info.eval)
      case "alignedCols" => HSPS.sortBy(_.info.aligned_cols)
      case "templateRef" => HSPS.sortBy(_.template.ref)
      case _             => HSPS.sortBy(_.num)
    }
    if (desc) l.reverse else l
  }

  // TODO remove
  def hitsOrderBy(params: DTParam): List[HHBlitsHSP] = List()
}

object HHBlitsResult {

  implicit val hhblitsResultDecoder: Decoder[HHBlitsResult] = (c: HCursor) =>
    for {
      hits       <- c.downField("results").downField("hits").as[List[Json]]
      alignments <- c.downField("results").downField("alignments").as[List[Json]]
      db         <- c.downField("results").downField("db").as[String]
      alignment  <- c.downField("reduced").as[AlignmentResult]
      query      <- c.downField("query").as[SingleSeq]
      tmpred     <- c.downField("results").downField("TMPRED").as[Option[String]]
      coilpred   <- c.downField("results").downField("COILPRED").as[Option[String]]
    } yield {
      val hspList = HHBlitsHSP.hhblitsHSPListDecoder(hits, alignments)
      new HHBlitsResult(hspList, alignment, hspList.length, query, db, tmpred.getOrElse("0"), coilpred.getOrElse("1"))
  }

}
