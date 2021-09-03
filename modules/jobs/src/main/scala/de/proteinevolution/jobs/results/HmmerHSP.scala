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

import io.circe.syntax._
import io.circe.{ Decoder, HCursor, Json }

case class HmmerHSP(
    eValue: Double,
    full_evalue: Double,
    num: Int,
    bitScore: Double,
    hit_start: Int,
    hit_end: Int,
    hit_seq: String,
    query_seq: String,
    query_start: Int,
    query_end: Int,
    query_id: String,
    hit_len: Int,
    accession: String,
    midline: String,
    description: String,
    domain_obs_num: Int
) extends HSP {

  def toTableJson(db: String): Json = {
    Map[String, Json](
      "numCheck" -> num.asJson,
      "acc"      -> LinkUtil.getSingleLinkDB(db, accession).asJson,
      "name"     -> description.slice(0, 84).asJson,
      "fullEval" -> full_evalue.asJson,
      "eval"     -> eValue.asJson,
      "bitScore" -> bitScore.asJson,
      "hitLen"   -> hit_len.asJson
    ).asJson
  }

  def toAlignmentSectionJson(db: String): Json = {
    Map[String, Json](
      "num"             -> num.asJson,
      "acc"             -> LinkUtil.getSingleLinkDB(db, accession).asJson,
      "fastaLink"       -> LinkUtil.getLinksDB(db, accession).asJson,
      "name"            -> description.asJson,
      "fullEval"        -> full_evalue.asJson,
      "eval"            -> eValue.asJson,
      "bitScore"        -> bitScore.asJson,
      "hitLen"          -> hit_len.asJson,
      "observedDomains" -> domain_obs_num.asJson,
      "agree"           -> midline.asJson,
      "query" -> Map(
        "start" -> query_start.asJson,
        "end"   -> query_end.asJson,
        "seq"   -> query_seq.asJson
      ).asJson,
      "template" -> Map(
        "start" -> hit_start.asJson,
        "end"   -> hit_end.asJson,
        "seq"   -> hit_seq.asJson
      ).asJson
    ).asJson
  }

}

object HmmerHSP {

  implicit val hmmerHSPDecoder: Decoder[HmmerHSP] =
    (c: HCursor) =>
      for {
        evalue         <- c.downField("evalue").as[Double]
        full_evalue    <- c.downField("full_evalue").as[Double]
        num            <- c.downField("num").as[Int]
        bitScore       <- c.downField("bitscore").as[Double]
        hit_start      <- c.downField("hit_start").as[Int]
        hit_end        <- c.downField("hit_end").as[Int]
        hit_seq        <- c.downField("hit_seq").as[String]
        query_seq      <- c.downField("query_seq").as[String]
        query_start    <- c.downField("query_start").as[Int]
        query_end      <- c.downField("query_end").as[Int]
        query_id       <- c.downField("query_id").as[String]
        hit_len        <- c.downField("hit_len").as[Int]
        hit_id         <- c.downField("hit_id").as[String]
        midline        <- c.downField("aln_ann").downField("PP").as[String]
        description    <- c.downField("hit_description").as[String]
        domain_obs_num <- c.downField("domain_obs_num").as[Int]
      } yield {
        val accession = General.refineAccession(hit_id)
        new HmmerHSP(
          evalue,
          full_evalue,
          num,
          bitScore,
          hit_start + 1,
          hit_end,
          hit_seq,
          query_seq,
          query_start + 1,
          query_end,
          query_id,
          hit_len,
          accession,
          midline.toUpperCase,
          description,
          domain_obs_num
        )
      }

}
