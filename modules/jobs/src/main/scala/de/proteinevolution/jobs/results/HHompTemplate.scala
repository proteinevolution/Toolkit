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

import io.circe.{ Decoder, HCursor }

case class HHompTemplate(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    ss_conf: String,
    ss_dssp: String,
    ss_pred: String,
    bb_pred: String,
    bb_conf: String,
    start: Int
)

object HHompTemplate {

  implicit def hhompTemplateDecoder(struct: String): Decoder[HHompTemplate] =
    (c: HCursor) =>
      for {
        consensus <- c.downField("consensus").as[String]
        end       <- c.downField("end").as[Int]
        ref       <- c.downField("ref").as[Int]
        seq       <- c.downField("seq").as[String]
        start     <- c.downField("start").as[Int]
        ss_dssp   <- c.downField("ss_dssp").as[String]
        ss_pred   <- c.downField("ss_pred").as[String]
        ss_conf   <- c.downField("ss_conf").as[String]
        bb_pred   <- c.downField("bb_pred").as[String]
        bb_conf   <- c.downField("bb_conf").as[String]
      } yield {
        val accession = General.refineAccession(struct)
        new HHompTemplate(consensus, end, accession, ref, seq, ss_conf, ss_dssp, ss_pred, bb_pred, bb_conf, start)
    }

}
