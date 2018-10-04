package de.proteinevolution.models.parameters

import cats.syntax.functor._
import de.proteinevolution.models.parameters.Parameter.TextAreaInputType.TextAreaInputType
import io.circe.generic.auto._
import io.circe.syntax._
import io.circe.{ Decoder, Encoder }

sealed trait Parameter {
  val name: String
  val label: String
}

object Parameter {

  case class TextInputParameter(
      override val name: String,
      override val label: String,
      inputPlaceholder: String
  ) extends Parameter

  object TextAreaInputType extends Enumeration {
    type TextAreaInputType = Value
    val SEQUENCE     = Value("sequence")
    val REGEX        = Value("regex")
    val PDB          = Value("pdb")
    val ACCESSION_ID = Value("accessionID")
  }

  case class TextAreaParameter(
      override val name: String,
      inputType: TextAreaInputType,
      inputPlaceholder: String,
      allowsTwoTextAreas: Boolean = false
  ) extends Parameter {
    override val label: String = ""
  }

  case class SelectOption(value: String, text: String)

  case class SelectParameter(
      override val name: String,
      override val label: String,
      options: Seq[SelectOption],
      maxSelectedOptions: Int
  ) extends Parameter

  case class NumberParameter(
      override val name: String,
      override val label: String,
      min: Option[Double] = None,
      max: Option[Double] = None,
      step: Option[Double] = None,
      default: Option[Double] = None
  ) extends Parameter

  case class BooleanParameter(
      override val name: String,
      override val label: String,
      default: Option[Boolean]
  ) extends Parameter

  case class ModellerParameter(
      override val name: String,
      override val label: String,
  ) extends Parameter

  implicit val encodeParameter: Encoder[Parameter] = Encoder.instance {
    case input @ TextInputParameter(_, _, _)        => input.asJson
    case area @ TextAreaParameter(_, _, _, _)       => area.asJson
    case select @ SelectParameter(_, _, _, _)       => select.asJson
    case number @ NumberParameter(_, _, _, _, _, _) => number.asJson
    case boolean @ BooleanParameter(_, _, _)        => boolean.asJson
    case modeller @ ModellerParameter(_, _)         => modeller.asJson
  }

  implicit val decodeParameter: Decoder[Parameter] = List[Decoder[Parameter]](
    Decoder[TextInputParameter].widen,
    Decoder[TextAreaParameter].widen,
    Decoder[SelectParameter].widen,
    Decoder[NumberParameter].widen,
    Decoder[BooleanParameter].widen,
    Decoder[ModellerParameter].widen
  ).reduceLeft(_.or(_))

}
