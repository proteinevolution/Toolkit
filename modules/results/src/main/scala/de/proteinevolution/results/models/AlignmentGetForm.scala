package de.proteinevolution.results.models

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class AlignmentGetForm(resultName: String, checkboxes: List[Int])

object AlignmentGetForm {

  implicit val resultsFormDecoder: Decoder[AlignmentGetForm] = deriveDecoder[AlignmentGetForm]

}
