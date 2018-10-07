package de.proteinevolution.models.parameters

case class ParameterSection(
    name: String,
    multiColumnLayout: Boolean,
    parameters: Seq[Parameter]
)
