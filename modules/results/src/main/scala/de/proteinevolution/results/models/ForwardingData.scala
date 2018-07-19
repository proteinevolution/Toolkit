package de.proteinevolution.results.models

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class ForwardingData(
    fileName: Option[String],
    evalue: Option[String],
    resultName: Option[String],
    checkboxes: Array[Int]
)

object ForwardingData {

  implicit val forwardingDataDecoder: Decoder[ForwardingData] =
    deriveDecoder[ForwardingData]

}
