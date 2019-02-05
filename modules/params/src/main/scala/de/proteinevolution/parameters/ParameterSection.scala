package de.proteinevolution.parameters

case class ParameterSection(
    name: String,
    multiColumnLayout: Boolean,
    parameters: Seq[Parameter]
)
