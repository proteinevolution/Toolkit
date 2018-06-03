package de.proteinevolution.tools.models

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class ResultForm(
    start: Int,
    end: Int,
    wrapped: Option[Boolean],
    isColor: Option[Boolean],
    resultName: Option[String]
)

object ResultForm {

  implicit val resultFormDecoder: Decoder[ResultForm] = deriveDecoder[ResultForm]

}
