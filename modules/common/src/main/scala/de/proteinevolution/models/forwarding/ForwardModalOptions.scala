package de.proteinevolution.models.forwarding

import play.api.libs.json.JsonConfiguration.Aux
import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{ Json, JsonConfiguration, OFormat }

case class ForwardModalOptions(heading: String,
                               showControlArea: Boolean,
                               showRadioBtnSelection: Boolean,
                               showRadioBtnSequenceLength: Boolean,
                               alignmentOptions: Array[String],
                               multiSeqOptions: Array[String])

object ForwardModalOptions {

  implicit val config: Aux[Json.MacroOptions] = JsonConfiguration(SnakeCase)

  implicit val forwardModalOptionsWrites: OFormat[ForwardModalOptions] = Json.format[ForwardModalOptions]

}
