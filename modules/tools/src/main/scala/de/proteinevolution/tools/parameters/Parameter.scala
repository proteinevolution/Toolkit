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

package de.proteinevolution.tools.parameters

import io.circe.Encoder
import io.circe.generic.extras.Configuration

sealed trait Parameter {
  def name: String
}

object Parameter {

  case class TextInputParameter(
      name: String,
      inputPlaceholder: String,
      regex: Option[String] = None,
      sampleInput: Option[String] = None,
      disableRemember: Option[Boolean] = None
  ) extends Parameter

  case class TextAreaParameter(
      name: String,
      inputType: String,
      placeholderKey: String,
      sampleInputKey: String,
      allowsTwoTextAreas: Boolean = false
  ) extends Parameter

  case class SelectOption(value: String, text: String)

  case class SelectParameter(
      name: String,
      default: Option[String],
      options: Seq[SelectOption],
      maxSelectedOptions: Int,
      onDetectedMSA: Option[String]
  ) extends Parameter

  case class NumberParameter(
      name: String,
      min: Option[Double] = None,
      max: Option[Double] = None,
      step: Option[Double] = None,
      default: Option[Double] = None
  ) extends Parameter

  case class BooleanParameter(
      name: String,
      default: Option[Boolean]
  ) extends Parameter

  case class ModellerParameter(
      name: String,
      label: String,
  ) extends Parameter

  case class HHpredSelectsParameter(
    name: String,
    options: Seq[SelectOption],
    nameProteomes: String,
    optionsProteomes: Seq[SelectOption],
    maxSelectedOptions: Int,
    default: Option[String] = None,
    defaultProteomes: Option[String] = None,
  ) extends Parameter

  implicit val ep: Encoder[Parameter] = {
    import io.circe.generic.extras.auto._

    implicit val config: Configuration =
      io.circe.generic.extras.defaults.defaultGenericConfiguration.copy(discriminator = Some("parameterType"))
    val _ = config
    io.circe.generic.extras.semiauto.deriveConfiguredEncoder[Parameter]

  }

}
