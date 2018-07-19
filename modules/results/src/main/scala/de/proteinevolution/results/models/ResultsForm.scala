package de.proteinevolution.results.models

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class ResultsForm(
    start: Int,
    end: Int,
    wrapped: Option[Boolean],
    isColor: Option[Boolean],
    resultName: Option[String]
)

object ResultsForm {

  implicit val resultsFormDecoder: Decoder[ResultsForm] =
    deriveDecoder[ResultsForm]

}
