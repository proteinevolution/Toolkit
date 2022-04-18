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

import io.circe._
import io.circe.syntax._

case class HHBlitsHSP(
    query: HHBlitsQuery,
    template: HHBlitsTemplate,
    info: HHBlitsInfo,
    agree: String,
    description: String,
    num: Int,
    length: Int
) extends HSP {

  def toTableJson(db: String): Json = {
    Map[String, Json](
      "numCheck"    -> num.asJson,
      "acc"         -> LinkUtil.getSingleLinkHHBlits(template.accession).asJson,
      "name"        -> description.asJson,
      "probab"      -> info.probab.asJson,
      "eval"        -> info.eval.asJson,
      "alignedCols" -> info.aligned_cols.asJson,
      "templateRef" -> template.ref.asJson
    ).asJson
  }

  def toAlignmentSectionJson(db: String): Json = {
    Map[String, Json](
      "num"         -> num.asJson,
      "acc"         -> LinkUtil.getSingleLinkHHBlits(template.accession).asJson,
      "name"        -> description.asJson,
      "probab"      -> info.probab.asJson,
      "eval"        -> info.eval.asJson,
      "score"       -> info.score.asJson,
      "alignedCols" -> info.aligned_cols.asJson,
      "identities"  -> (info.identities * 100).asJson,
      "similarity"  -> info.similarity.asJson,
      "query"       -> query.asJson,
      "agree"       -> agree.asJson,
      "template"    -> template.asJson
    ).asJson
  }

  override val eValue: Double    = info.eval
  override val accession: String = template.accession

}

object HHBlitsHSP {

  implicit def hhblitsHSPDecoder(struct: String): Decoder[HHBlitsHSP] =
    (c: HCursor) =>
      for {
        queryResult    <- c.downField("query").as[HHBlitsQuery]
        infoResult     <- c.downField("info").as[HHBlitsInfo]
        templateResult <- c.downField("template").as[HHBlitsTemplate](HHBlitsTemplate.hhblitsTemplateDecoder(struct))
        agree          <- c.downField("agree").as[String]
        description    <- c.downField("header").as[String]
        num            <- c.downField("no").as[Int]
      } yield new HHBlitsHSP(
        queryResult,
        templateResult,
        infoResult,
        agree,
        description,
        num,
        agree.length
      )

  def hhblitsHSPListDecoder(hits: List[Json], alignments: List[Json]): List[HHBlitsHSP] = {
    alignments.zip(hits).flatMap { case (a, h) =>
      (for {
        struct <- h.hcursor.downField("struc").as[String]
        hsp    <- a.hcursor.as[HHBlitsHSP](hhblitsHSPDecoder(struct))
      } yield hsp).toOption
    }
  }

}
