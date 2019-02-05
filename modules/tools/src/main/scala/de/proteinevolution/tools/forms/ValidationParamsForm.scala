package de.proteinevolution.tools.forms

import play.api.libs.json.{ Json, OWrites }

case class ValidationParamsForm(
    allowedSeqFormats: Seq[String],
    allowedSeqType: String
)

object ValidationParamsForm {

  implicit val writes: OWrites[ValidationParamsForm] = Json.writes[ValidationParamsForm]

}
