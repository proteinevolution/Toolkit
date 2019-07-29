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
import io.circe.{ Decoder, HCursor, Json }

case class HHPredResult(
    HSPS: List[HHPredHSP],
    alignment: AlignmentResult,
    num_hits: Int,
    query: SingleSeq,
    db: String,
    proteomes: String,
    TMPRED: String,
    COILPRED: String,
    MSA_GEN: String,
    QA3M_COUNT: Int
) extends SearchResult[HHPredHSP] {

  def hitsOrderBy(sortBy: String, desc: Boolean): List[HHPredHSP] = {
    val l = sortBy match {
      case "acc"         => HSPS.sortBy(_.template.accession)
      case "name"        => HSPS.sortBy(_.description)
      case "probab"      => HSPS.sortBy(_.info.probab)
      case "eval"        => HSPS.sortBy(_.info.eval)
      case "ssScore"     => HSPS.sortBy(_.ss_score)
      case "alignedCols" => HSPS.sortBy(_.info.aligned_cols)
      case "templateRef" => HSPS.sortBy(_.template.ref)
      case _             => HSPS.sortBy(_.num)
    }
    if (desc) l.reverse else l
  }

}

object HHPredResult {

  implicit val hhpredResultDecoder: Decoder[HHPredResult] = (c: HCursor) =>
    for {
      hits       <- c.downField("results").downField("hits").as[List[Json]]
      alignments <- c.downField("results").downField("alignments").as[List[Json]]
      db         <- c.downField("results").downField("db").as[String]
      alignment  <- c.downField("reduced").as[AlignmentResult]
      query      <- c.downField("query").as[SingleSeq]
      proteomes  <- c.downField("results").downField("proteomes").as[String]
      tmpred     <- c.downField("results").downField("TMPRED").as[Option[String]]
      coilpred   <- c.downField("results").downField("COILPRED").as[Option[String]]
      msa_gen    <- c.downField("results").downField("MSA_GEN").as[Option[String]]
      qa3m_count <- c.downField("results").downField("QA3M_COUNT").as[Option[Int]]
    } yield {
      val hspList = HHPredHSP.hhpredHSPListDecoder(hits, alignments)
      new HHPredResult(
        hspList,
        alignment,
        hspList.length,
        query,
        db,
        proteomes,
        tmpred.getOrElse("0"),
        coilpred.getOrElse("1"),
        msa_gen.getOrElse(""),
        qa3m_count.getOrElse(1)
      )
    }

}
