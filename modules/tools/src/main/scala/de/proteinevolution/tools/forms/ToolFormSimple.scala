package de.proteinevolution.tools.forms

import play.api.libs.json._

case class ToolFormSimple(
    name: String,
    longname: String,
    description: String,
    section: String,
    version: String,
    validationParams: ValidationParamsForm
)

object ToolFormSimple {

  implicit val writes: OWrites[ToolFormSimple] = Json.writes[ToolFormSimple]

}
