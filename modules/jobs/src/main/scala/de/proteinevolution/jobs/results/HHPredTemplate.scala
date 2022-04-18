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

import io.circe.generic.semiauto.deriveEncoder
import io.circe.{ Decoder, Encoder, HCursor }

case class HHPredTemplate(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    ss_dssp: String,
    ss_pred: String,
    start: Int
)

object HHPredTemplate {

  implicit def hhpredTemplateDecoder(struct: String): Decoder[HHPredTemplate] =
    (c: HCursor) =>
      for {
        consensus <- c.downField("consensus").as[String]
        end       <- c.downField("end").as[Int]
        ref       <- c.downField("ref").as[Int]
        seq       <- c.downField("seq").as[String]
        ss_dssp   <- c.downField("ss_dssp").as[String]
        ss_pred   <- c.downField("ss_pred").as[String]
        start     <- c.downField("start").as[Int]
      } yield {
        val accession = General.refineAccession(struct)
        new HHPredTemplate(
          consensus,
          end,
          accession,
          ref,
          seq,
          ss_dssp,
          ss_pred,
          start
        )
      }
  implicit val hhpredTemplateEncoder: Encoder[HHPredTemplate] = deriveEncoder

}
