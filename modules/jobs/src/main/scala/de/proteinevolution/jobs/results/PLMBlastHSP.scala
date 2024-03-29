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

import io.circe.syntax._
import io.circe.{ Decoder, HCursor, Json }

case class PLMBlastHSP(
    num: Int,
    eValue: Double,
    identScore: Double,
    hit_start: Int,
    hit_end: Int,
    match_len: Int,
    query_start: Int,
    query_end: Int,
    hit_len: Int,
    accession: String,
    description: String
) extends HSP {

  def toTableJson(db: String): Json = {
    Map[String, Json](
      "num"      -> num.asJson,
      "acc"      -> LinkUtil.getSingleLink(accession).asJson,
      "name"     -> description.slice(0, 300).asJson,
      "eval"     -> eValue.asJson,
      "bitScore" -> ((identScore * 100).round.toString + "%").asJson,
      "hitLen"   -> hit_len.asJson,
      "matchLen" -> match_len.asJson
    ).asJson
  }

  def toAlignmentSectionJson(db: String): Json = {
    Map[String, Json](
      "num"      -> num.asJson,
      "acc"      -> LinkUtil.getSingleLink(accession).asJson,
      "name"     -> description.asJson,
      "eval"     -> eValue.asJson,
      "bitScore" -> (identScore * 100).round.asJson,
      "hitLen"   -> hit_len.asJson,
      "query" -> Map(
        "start" -> query_start.asJson,
        "end"   -> query_end.asJson
      ).asJson,
      "template" -> Map(
        "start" -> hit_start.asJson,
        "end"   -> hit_end.asJson
      ).asJson
    ).asJson
  }

}

object PLMBlastHSP {

  implicit val plmblastHSPDecoder: Decoder[PLMBlastHSP] =
    (c: HCursor) =>
      for {
        num         <- c.downField("index").as[Int]
        evalue      <- c.downField("score").as[Double]
        identScore  <- c.downField("ident").as[Double]
        hit_start   <- c.downField("tstart").as[Int]
        hit_end     <- c.downField("tend").as[Int]
        match_len   <- c.downField("match_len").as[Int]
        query_start <- c.downField("qstart").as[Int]
        query_end   <- c.downField("qend").as[Int]
        hit_len     <- c.downField("tlen").as[Int]
        hit_id      <- c.downField("sid").as[String]
        description <- c.downField("sdesc").as[String]
      } yield {
        val accession = General.refineAccession(hit_id)
        new PLMBlastHSP(
          num,
          evalue,
          identScore,
          hit_start,
          hit_end,
          match_len,
          query_start,
          query_end,
          hit_len,
          accession,
          description
        )
      }

}
