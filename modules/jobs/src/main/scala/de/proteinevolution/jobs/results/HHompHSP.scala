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

import io.circe._
import io.circe.syntax._

case class HHompHSP(
    query: HHompQuery,
    template: HHompTemplate,
    info: HHompInfo,
    agree: String,
    description: String,
    num: Int,
    ss_score: Double,
    length: Int
) extends HSP {

  def toTableJson(db: String): Json = {
    Map[String, Json](
      "num"         -> num.asJson,
      "acc"         -> template.accession.asJson,
      "name"        -> description.slice(0, 18).asJson,
      "probabHit"   -> info.probab_hit.asJson,
      "probabOMP"   -> info.probab_OMP.asJson,
      "eval"        -> info.eval.asJson,
      "ssScore"     -> ss_score.asJson,
      "alignedCols" -> info.aligned_cols.asJson,
      "templateRef" -> template.ref.asJson
    ).asJson
  }

  def toAlignmentSectionJson(db: String): Json = {
    Map[String, Json](
      "num"         -> num.asJson,
      "acc"         -> template.accession.asJson,
      "name"        -> description.asJson,
      "probabHit"   -> info.probab_hit.asJson,
      "probabOMP"   -> info.probab_OMP.asJson,
      "eval"        -> info.eval.asJson,
      "score"       -> info.score.asJson,
      "alignedCols" -> info.aligned_cols.asJson,
      "identities"  -> info.identities.asJson,
      "query"       -> query.asJson,
      "template"    -> template.asJson,
      "agree"       -> agree.asJson,
    ).asJson
  }

  override val accession: String = template.accession

  override val eValue: Double = info.eval

}

object HHompHSP {

  implicit def hhompHSPDecoder(struct: String, ss_score: Double): Decoder[HHompHSP] =
    (c: HCursor) =>
      for {
        queryResult    <- c.downField("query").as[HHompQuery]
        infoResult     <- c.downField("info").as[HHompInfo]
        templateResult <- c.downField("template").as[HHompTemplate](HHompTemplate.hhompTemplateDecoder(struct))
        agree          <- c.downField("agree").as[String]
        description    <- c.downField("header").as[String]
        num            <- c.downField("no").as[Int]
      } yield new HHompHSP(
        queryResult,
        templateResult,
        infoResult,
        agree,
        description,
        num,
        ss_score,
        agree.length
      )

  def hhompHSPListDecoder(hits: List[Json], alignments: List[Json]): List[HHompHSP] = {
    alignments.zip(hits).flatMap {
      case (a, h) =>
        (for {
          struct   <- h.hcursor.downField("struc").as[String]
          ss_score <- h.hcursor.downField("ss").as[Double]
          hsp      <- a.hcursor.as[HHompHSP](hhompHSPDecoder(struct, ss_score))
        } yield hsp).toOption
    }
  }

}
