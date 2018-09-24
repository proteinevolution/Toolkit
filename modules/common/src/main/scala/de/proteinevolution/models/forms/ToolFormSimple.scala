package de.proteinevolution.models.forms

import play.api.libs.json._

case class ToolFormSimple(
    name: String,
    longname: String,
    description: String,
    section: String,
    validationParams: ValidationParamsForm
)

object ToolFormSimple {

  implicit val writes: OWrites[ToolFormSimple] = Json.writes[ToolFormSimple]

}
