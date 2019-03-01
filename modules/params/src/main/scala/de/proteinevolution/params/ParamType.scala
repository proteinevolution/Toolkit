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

package de.proteinevolution.params

import io.circe._
import io.circe.syntax._

sealed trait ParamType {

  /**
   * Parses the value and return the same value as Option if valid, otherwise None
   * @param value String value to be validated
   * @return Some(value) if value is valid, else None
   */
  def validate(value: String): Option[String]
}

object ParamType {

  case class Sequence(formats: Seq[(String, String)], placeholder: String, allowTwoTextAreas: Boolean)
      extends ParamType {
    // Sequence currently alwasus valid
    def validate(value: String): Option[String] = Some(value)
  }

  case class Number(min: Option[Int], max: Option[Int]) extends ParamType {
    def validate(value: String): Option[String] = {
      if (min.getOrElse(Integer.MIN_VALUE) <= value.toInt && value.toInt <= max.getOrElse(Integer.MAX_VALUE))
        Some(value)
      else
        None
    }
  }
  case class Select(options: Seq[(String, String)]) extends ParamType {
    def validate(value: String): Option[String] = {
      Some(value).filter(options.map {
        case (key, _) => key
      }.contains)
    }
  }

  case object Bool extends ParamType {
    def validate(value: String): Option[String] = {
      Some(value)
    }
  }

  case object Radio extends ParamType {
    def validate(value: String): Option[String] = {
      Some(value)
    }
  }
  case class Decimal(step: String, min: Option[Double], max: Option[Double]) extends ParamType {
    def validate(value: String): Option[String] = {
      if (min.getOrElse(Double.MinValue) <= value.toDouble && value.toDouble <= max.getOrElse(Double.MaxValue))
        Some(value)
      else
        None
    }
  }

  case class Text(placeholder: String = "") extends ParamType {
    def validate(value: String): Option[String] = Some(value)
  }

  case object ModellerKey extends ParamType {
    def validate(value: String): Option[String] = Some(value)
  }

  final val UnconstrainedNumber = Number(None, None)
  final val Percentage          = Number(Some(0), Some(100))
  final val ConstrainedNumber   = Number(Some(1), Some(10000))
  final val FIELD_TYPE          = "type"

  implicit val paramTypeEncoder: Encoder[ParamType] = Encoder.instance {
    case Bool  => JsonObject(FIELD_TYPE -> Json.fromInt(4)).asJson
    case Radio => JsonObject(FIELD_TYPE -> Json.fromInt(5)).asJson
    case Decimal(step, minVal, maxVal) =>
      JsonObject(
        FIELD_TYPE -> Json.fromInt(2),
        "step"     -> Json.fromString(step),
        "min"      -> minVal.map(Json.fromDoubleOrNull).getOrElse(Json.Null),
        "max"      -> maxVal.map(Json.fromDoubleOrNull).getOrElse(Json.Null)
      ).asJson
    case Text(placeholder) =>
      JsonObject(
        FIELD_TYPE    -> Json.fromInt(7),
        "placeholder" -> Json.fromString(placeholder)
      ).asJson
    case Number(minOpt, maxOpt) =>
      JsonObject(
        FIELD_TYPE -> Json.fromInt(2),
        "min"      -> minOpt.map(Json.fromInt).getOrElse(Json.Null),
        "max"      -> maxOpt.map(Json.fromInt).getOrElse(Json.Null)
      ).asJson
    case Select(options) =>
      JsonObject(
        FIELD_TYPE -> Json.fromInt(3),
        "options"  -> options.asJson
      ).asJson
    case ModellerKey =>
      JsonObject(
        FIELD_TYPE -> Json.fromInt(8)
      ).asJson
    case Sequence(formats, placeholder, allowTwoTextAreas) =>
      JsonObject(
        FIELD_TYPE           -> Json.fromInt(1),
        "modes"              -> formats.asJson,
        "allowsTwoTextAreas" -> Json.fromBoolean(allowTwoTextAreas),
        "placeholder"        -> Json.fromString(placeholder)
      ).asJson
    case _ => throw new Exception // why is this even necessary?
  }

  // temporary hack, needed in order to use the @JsonCodec annotation for the Param case class
  implicit val paramTypeDecoder: Decoder[ParamType] = (_: HCursor) => {
    Right(new ParamType {
      override def validate(value: String): Option[String] = Option("error")
    })
  }

}
