package de.proteinevolution.parameters

case class ToolParameters(
    sections: Seq[ParameterSection],
    forwarding: ForwardingMode
)
