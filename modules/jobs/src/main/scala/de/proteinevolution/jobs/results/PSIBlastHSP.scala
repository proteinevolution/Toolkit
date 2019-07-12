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

  import SearchResultImplicits._

  def toDataTable(db: String): Json = {
    Map[String, Either[Double, String]](
      "0" -> Right(Common.getCheckbox(num)),
      "1" -> Right(Common.getSingleLinkDB(db, accession).toString),
      "2" -> Right(Common.addBreak(description.slice(0, 84))),
      "3" -> Right("%.2e".format(eValue)),
      "4" -> Left(bitScore),
      "5" -> Left(ref_len.toDouble),
      "6" -> Left(hit_len.toDouble)
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
      // workaround: bug of psiblast output when searching pdb_nr
      val accessionString = if (db == "pdb_nr") {
        description.split("\\s+").headOption.getOrElse("")
      } else {
        General.refineAccession(accession)
      }
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
        accessionString,
        midLine.toUpperCase,
        description
      )
    }
  }

}
