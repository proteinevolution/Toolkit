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

import io.circe.Json
import io.circe.syntax._

case class PSIBlastHSP(
    eValue: Double,
    num: Int,
    bitScore: Double,
    score: Int,
    hit_start: Int,
    hit_end: Int,
    hit_seq: String,
    query_seq: String,
    query_start: Int,
    query_end: Int,
    hit_len: Int,
    gaps: Int,
    identity: Int,
    positive: Int,
    ref_len: Int,
    accession: String,
    midLine: String,
    description: String
) extends HSP {

  def toTableJson(db: String): Json = {
    Map[String, Json](
      "numCheck" -> num.asJson,
      "acc"      -> LinkUtil.getSingleLinkDB(db, accession).asJson,
      "name"     -> description.slice(0, 84).asJson,
      "eval"     -> "%.2e".format(eValue).asJson,
      "bitScore" -> bitScore.asJson,
      "refLen"   -> ref_len.asJson,
      "hitLen"   -> hit_len.asJson
    ).asJson
  }

  def toAlignmentSectionJson(db: String): Json = {
    // TODO adapt
    Map[String, Json](
      "num"       -> num.asJson,
      "acc"       -> LinkUtil.getSingleLinkDB(db, accession).asJson,
      "fastaLink" -> LinkUtil.getLinksDB(db, accession).asJson,
      "name"      -> description.slice(0, 84).asJson,
      "eval"      -> "%.2e".format(eValue).asJson,
      "score"     -> score.asJson,
      "bitScore"  -> bitScore.asJson,
      "ident"     -> identity.asJson,
      "perIdent"  -> "%1.0f".format(((identity.toFloat / hit_len) * 100)).asJson,
      "pos"       -> positive.asJson,
      "perPos"    -> "%1.0f".format(((positive.toFloat / hit_len) * 100)).asJson,
      "gap"       -> gaps.asJson,
      "perGap"    -> "%1.0f".format(((gaps.toFloat / hit_len) * 100)).asJson,
      "refLen"    -> ref_len.asJson,
      "hitLen"    -> hit_len.asJson,
      "agree"     -> midLine.asJson,
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

object PSIBlastHSP {

  import io.circe.{ Decoder, HCursor }

  def parseHSP(db: String): Decoder[PSIBlastHSP] = (c: HCursor) => {
    val hsps            = c.downField("hsps").downArray.first
    val descriptionBase = c.downField("description").downArray.first
    for {
      eValue      <- hsps.downField("evalue").as[Double]
      num         <- c.downField("num").as[Int]
      bitScore    <- hsps.downField("bit_score").as[Double]
      score       <- hsps.downField("score").as[Int]
      positive    <- hsps.downField("positive").as[Int]
      identity    <- hsps.downField("identity").as[Int]
      gaps        <- hsps.downField("gaps").as[Int]
      hit_start   <- hsps.downField("hit_from").as[Int]
      hit_end     <- hsps.downField("hit_to").as[Int]
      hit_seq     <- hsps.downField("hseq").as[String]
      query_seq   <- hsps.downField("qseq").as[String]
      query_start <- hsps.downField("query_from").as[Int]
      query_end   <- hsps.downField("query_to").as[Int]
      ref_len     <- c.downField("len").as[Int]
      hit_len     <- hsps.downField("align_len").as[Int]
      midLine     <- hsps.downField("midline").as[String]
      description <- descriptionBase.downField("title").as[String]
      accession   <- descriptionBase.downField("accession").as[String]
    } yield {
      PSIBlastHSP(
        eValue,
        num,
        bitScore,
        score,
        hit_start,
        hit_end,
        hit_seq.toUpperCase,
        query_seq.toUpperCase,
        query_start,
        query_end,
        hit_len,
        gaps,
        identity,
        positive,
        ref_len,
        accession,
        midLine.toUpperCase,
        description
      )
    }
  }

}
