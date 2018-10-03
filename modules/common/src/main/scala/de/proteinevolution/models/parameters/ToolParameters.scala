package de.proteinevolution.models.parameters

case class ToolParameters(
    sections: Seq[ParameterSection],
    forwarding: ForwardingMode
)
