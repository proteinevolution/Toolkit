package de.proteinevolution.models.forwarding

import io.circe.generic.extras._

@ConfiguredJsonCodec case class ForwardModalOptions(
    heading: String,
    showRadioBtnSelection: Boolean,
    showRadioBtnSequenceLength: Boolean,
    alignmentOptions: Array[String],
    multiSeqOptions: Array[String]
)

object ForwardModalOptions {

  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

}
