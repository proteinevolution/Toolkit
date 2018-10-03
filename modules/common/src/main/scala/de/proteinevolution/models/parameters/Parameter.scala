package de.proteinevolution.models.parameters

import de.proteinevolution.models.parameters.Parameter.TextAreaInputType.TextAreaInputType

class Parameter(
    val parameterType: String,
    val name: String,
    val label: String
)

object Parameter {

  case class TextInputParameter(
      override val name: String,
      override val label: String,
      inputPlaceholder: String
  ) extends Parameter("TextInput", name, label)

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
  ) extends Parameter("TextArea", name, "")

  case class SelectOption(value: String, text: String)

  case class SelectParameter(
      override val name: String,
      override val label: String,
      options: Seq[SelectOption],
      maxSelectedOptions: Int
  ) extends Parameter("Select", name, label)

  case class NumberParameter(
      override val name: String,
      override val label: String,
      min: Option[Double] = None,
      max: Option[Double] = None,
      step: Option[Double] = None,
      default: Option[Double] = None
  ) extends Parameter("Number", name, label)

  case class BooleanParameter(
      override val name: String,
      override val label: String,
      default: Option[Boolean]
  ) extends Parameter("Boolean", name, label)

  case class ModellerParameter(
      override val name: String,
      override val label: String,
  ) extends Parameter("ModellerKey", name, label)

}
