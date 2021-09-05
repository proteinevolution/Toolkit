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

package de.proteinevolution.tools.forms

import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._

sealed trait ValidationParamsForm

object ValidationParamsForm {

  case class SequenceValidationParamsForm(
      allowedSeqFormats: Seq[String],
      allowedSeqType: String,
      minCharPerSeq: Option[Int],
      maxCharPerSeq: Option[Int],
      minNumSeq: Option[Int],
      maxNumSeq: Option[Int],
      requiresSameLengthSeq: Option[Boolean],
      allowEmptySeq: Option[Boolean]
  ) extends ValidationParamsForm

  case class RegexValidationParamsForm(
      maxRegexLength: Int
  ) extends ValidationParamsForm

  case class AccessionIDValidationParamsForm(
      maxNumIDs: Int
  ) extends ValidationParamsForm

  case class EmptyValidationParamsForm() extends ValidationParamsForm

  // encode different cases without discriminator (https://circe.github.io/circe/codecs/adt.html)
  implicit val ep: Encoder[ValidationParamsForm] = Encoder.instance {
    case seq @ SequenceValidationParamsForm(_, _, _, _, _, _, _, _) => seq.asJson
    case regex @ RegexValidationParamsForm(_)                       => regex.asJson
    case acc @ AccessionIDValidationParamsForm(_)                   => acc.asJson
    case empty @ EmptyValidationParamsForm()                        => empty.asJson
  }

}
