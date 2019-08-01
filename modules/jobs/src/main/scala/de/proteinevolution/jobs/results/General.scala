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
import io.circe.generic.semiauto._

import scala.util.matching.Regex

object General {

  private final val accessionMalFormat: Regex = """.*\|(.*)\|.*""".r

  def refineAccession(seq: String): String = seq match {
    case accessionMalFormat(group) => group
    case _                         => seq
  }

  case class SingleSeq(accession: String, seq: String)

  object SingleSeq {

    implicit val decodeSingleSeq: Decoder[SingleSeq] = (c: HCursor) =>
      for {
        accession <- c.downArray.first.downArray.first.as[String]
        seq       <- c.downArray.first.downArray.right.as[String]
      } yield new SingleSeq(accession, seq)

    implicit val singleSeqEncoder: Encoder[SingleSeq] = deriveEncoder
  }

}
