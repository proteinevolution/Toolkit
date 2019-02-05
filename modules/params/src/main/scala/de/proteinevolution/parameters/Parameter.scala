package de.proteinevolution.parameters

import io.circe.Encoder
import io.circe.generic.extras.Configuration

sealed trait Parameter {
  def name: String
  def label: String
}

object Parameter {

  case class TextInputParameter(
      name: String,
      label: String,
      inputPlaceholder: String,
      regex: Option[String] = None,
      sampleInput: Option[String] = None
  ) extends Parameter

  case class TextAreaParameter(
      name: String,
      inputType: String,
      inputPlaceholder: String,
      sampleInputKey: String,
      allowsTwoTextAreas: Boolean = false
  ) extends Parameter {
    override val label: String = ""
  }

  case class SelectOption(value: String, text: String)

  case class SelectParameter(
      name: String,
      label: String,
      options: Seq[SelectOption],
      maxSelectedOptions: Int
  ) extends Parameter

  case class NumberParameter(
      name: String,
      label: String,
      min: Option[Double] = None,
      max: Option[Double] = None,
      step: Option[Double] = None,
      default: Option[Double] = None
  ) extends Parameter

  case class BooleanParameter(
      name: String,
      label: String,
      default: Option[Boolean]
  ) extends Parameter

  case class ModellerParameter(
      name: String,
      label: String,
  ) extends Parameter

  implicit val ep: Encoder[Parameter] = {
    import io.circe.generic.extras.auto._

    implicit val config: Configuration =
      io.circe.generic.extras.defaults.defaultGenericConfiguration.copy(discriminator = Some("parameterType"))
    val _ = config
    io.circe.generic.extras.semiauto.deriveEncoder[Parameter]

  }

}
