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

case class HHPredHSP(
    query: HHPredQuery,
    template: HHPredTemplate,
    info: HHPredInfo,
    agree: String,
    description: String,
    num: Int,
    ss_score: Double,
    confidence: String,
    length: Int
) extends HSP {

  def toTableJson(db: String = ""): Json = {
    Map[String, Json](
      "numCheck"    -> num.asJson,
      "acc"         -> LinkUtil.getSingleLink(accession).asJson,
      "name"        -> description.slice(0, 120).asJson,
      "probab"      -> info.probab.asJson,
      "eval"        -> info.eval.asJson,
      "score"       -> info.score.asJson,
      "ssScore"     -> ss_score.asJson,
      "alignedCols" -> info.aligned_cols.asJson,
      "templateRef" -> template.ref.asJson
    ).asJson
  }

  // TODO
  def toAlignmentSectionJson(db: String = ""): Json = {
    Map[String, Json](
      "num"           -> num.asJson,
      "acc"           -> LinkUtil.getSingleLink(accession).asJson,
      "structLink"    -> LinkUtil.displayStructLink(accession).asJson,
      "dbLink"        -> LinkUtil.getLinksHHpred("11", accession).asJson,
      "name"          -> description.asJson,
      "probab"        -> info.probab.asJson,
      "eval"          -> info.eval.asJson,
      "score"         -> info.score.asJson,
      "ssScore"       -> ss_score.asJson,
      "template_neff" -> info.template_neff.asJson,
      "ident"         -> "%1.0f".format(info.identities.toFloat * 100).asJson,
      "similarity"    -> info.similarity.asJson,
      "alignedCols"   -> info.aligned_cols.asJson,
      "template"      -> template.asJson,
      "agree"         -> agree.asJson,
      "query"         -> query.asJson
    ).asJson
  }

  override val eValue: Double    = info.eval
  override val accession: String = template.accession

}

object HHPredHSP {

  implicit def hhpredHSPDecoder(struct: String, ss_score: Double): Decoder[HHPredHSP] =
    (c: HCursor) =>
      for {
        queryResult    <- c.downField("query").as[HHPredQuery]
        infoResult     <- c.downField("info").as[HHPredInfo]
        templateResult <- c.downField("template").as[HHPredTemplate](HHPredTemplate.hhpredTemplateDecoder(struct))
        agree          <- c.downField("agree").as[String]
        description    <- c.downField("header").as[String]
        num            <- c.downField("no").as[Int]
        confidence     <- c.downField("confidence").as[Option[String]]
      } yield new HHPredHSP(
        queryResult,
        templateResult,
        infoResult,
        agree,
        description,
        num,
        ss_score,
        confidence.getOrElse(""),
        agree.length
      )

  def hhpredHSPListDecoder(hits: List[Json], alignments: List[Json]): List[HHPredHSP] = {
    alignments.zip(hits).flatMap { case (a, h) =>
      (for {
        struct   <- h.hcursor.downField("struc").as[Option[String]]
        ss_score <- h.hcursor.downField("ss").as[Option[Double]]
        hsp      <- a.hcursor.as[HHPredHSP](hhpredHSPDecoder(struct.getOrElse(""), ss_score.getOrElse(-1d)))
      } yield hsp).toOption
    }
  }

}
