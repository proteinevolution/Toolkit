package de.proteinevolution.models.forms

import de.proteinevolution.models.param.Param
import io.circe.generic.JsonCodec

@JsonCodec case class ToolForm(
    toolname: String,
    toolnameLong: String,
    toolnameAbbrev: String,
    category: String,
    params: Seq[(String, Seq[Param])]
)
