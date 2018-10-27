package de.proteinevolution.models.parameters

import io.circe.Encoder
import io.circe.generic.JsonCodec
import io.circe.generic.auto._
import io.circe.syntax._

sealed trait Parameter {
  def name: String
  def label: String
}

object Parameter {

  @JsonCodec case class TextInputParameter(
      name: String,
      label: String,
      inputPlaceholder: String
  ) extends Parameter

  @JsonCodec case class TextAreaParameter(
      name: String,
      inputType: String,
      inputPlaceholder: String,
      allowsTwoTextAreas: Boolean = false
  ) extends Parameter {
    override val label: String = ""
  }

  @JsonCodec case class SelectOption(value: String, text: String)

  @JsonCodec case class SelectParameter(
      name: String,
      label: String,
      options: Seq[SelectOption],
      maxSelectedOptions: Int
  ) extends Parameter

  @JsonCodec case class NumberParameter(
      name: String,
      label: String,
      min: Option[Double] = None,
      max: Option[Double] = None,
      step: Option[Double] = None,
      default: Option[Double] = None
  ) extends Parameter

  @JsonCodec case class BooleanParameter(
      name: String,
      label: String,
      default: Option[Boolean]
  ) extends Parameter

  @JsonCodec case class ModellerParameter(
      name: String,
      label: String,
  ) extends Parameter

  implicit val encodeParameter: Encoder[Parameter] = Encoder.instance {
    case input @ TextInputParameter(_, _, _)        => input.asJson
    case area @ TextAreaParameter(_, _, _, _)       => area.asJson
    case select @ SelectParameter(_, _, _, _)       => select.asJson
    case number @ NumberParameter(_, _, _, _, _, _) => number.asJson
    case boolean @ BooleanParameter(_, _, _)        => boolean.asJson
    case modeller @ ModellerParameter(_, _)         => modeller.asJson
  }

}
