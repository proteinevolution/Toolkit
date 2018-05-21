package de.proteinevolution.models.param

import play.api.libs.functional.syntax.{ unlift, _ }
import play.api.libs.json.{ JsPath, Writes }

// A simple parameter with name and a type
case class Param(name: String, paramType: ParamType, internalOrdering: Int, label: String)

object Param {
  implicit val paramWrites: Writes[Param] = (
    (JsPath \ "name").write[String] and
    (JsPath \ "paramType").write[ParamType] and
    (JsPath \ "internalOrdering").write[Int] and
    (JsPath \ "label").write[String]
  )(unlift(Param.unapply))
}
