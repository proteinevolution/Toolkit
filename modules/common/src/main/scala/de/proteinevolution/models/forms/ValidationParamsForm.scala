package de.proteinevolution.models.forms
import play.api.libs.json.{ Json, OWrites }

case class ValidationParamsForm(
    allowedSeqFormat: Seq[String]
)

object ValidationParamsForm {

  implicit val writes: OWrites[ValidationParamsForm] = Json.writes[ValidationParamsForm]

}
