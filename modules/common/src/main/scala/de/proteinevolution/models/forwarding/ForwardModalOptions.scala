package de.proteinevolution.models.forwarding

import play.api.libs.json.{ Json, OFormat }

case class ForwardModalOptions(heading: String,
                               showRadioBtnSelection: Boolean,
                               showRadioBtnSequenceLength: Boolean,
                               alignmentOptions: Array[String],
                               multiSeqOptions: Array[String])

object ForwardModalOptions {

  implicit val forwardModalOptionsWrites: OFormat[ForwardModalOptions] = Json.format[ForwardModalOptions]

}
