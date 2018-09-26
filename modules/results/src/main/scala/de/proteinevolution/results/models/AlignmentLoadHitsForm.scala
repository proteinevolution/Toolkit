package de.proteinevolution.results.models

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class AlignmentLoadHitsForm(start: Int, end: Int, resultName: String)

object AlignmentLoadHitsForm {

  implicit val resultsFormDecoder: Decoder[AlignmentLoadHitsForm] = deriveDecoder[AlignmentLoadHitsForm]

}
