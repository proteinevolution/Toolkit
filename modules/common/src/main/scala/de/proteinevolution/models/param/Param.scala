package de.proteinevolution.models.param

import play.api.libs.json._

// A simple parameter with name and a type
case class Param(
    name: String,
    paramType: ParamType,
    internalOrdering: Int,
    label: String
)

object Param {

  implicit val paramWrites: OWrites[Param] = Json.writes[Param]

}
