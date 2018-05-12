package de.proteinevolution.models.util

import play.api.libs.json.{ Json, Writes }

case class ForwardModalOptions(heading: String,
                               showRadioBtnSelection: Boolean,
                               showRadioBtnSequenceLength: Boolean,
                               alignmentOptions: Array[String],
                               multiSeqOptions: Array[String])

object ForwardModalOptions {
  implicit val forwardModalOptionsWrites: Writes[ForwardModalOptions] = Json.writes[ForwardModalOptions]
}
