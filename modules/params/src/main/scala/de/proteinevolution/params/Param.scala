package de.proteinevolution.params

import io.circe.generic.JsonCodec

@JsonCodec case class Param(
    name: String,
    paramType: ParamType,
    internalOrdering: Int,
    label: String
)
