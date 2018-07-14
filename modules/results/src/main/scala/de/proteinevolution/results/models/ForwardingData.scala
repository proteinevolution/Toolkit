package de.proteinevolution.results.models

import io.circe._, io.circe.generic.semiauto._

case class ForwardingData(
    fileName: Option[String],
    evalue: Option[String],
    resultName: Option[String],
    checkboxes: Array[Int]
)

object ForwardingData {

  implicit val forwardingDataDecoder: Decoder[ForwardingData] = deriveDecoder[ForwardingData]

}
