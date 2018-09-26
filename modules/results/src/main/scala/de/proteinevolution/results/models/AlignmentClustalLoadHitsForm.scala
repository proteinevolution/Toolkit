package de.proteinevolution.results.models
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class AlignmentClustalLoadHitsForm(resultName: String, color: Boolean)

object AlignmentClustalLoadHitsForm {

  implicit val resultsFormDecoder: Decoder[AlignmentClustalLoadHitsForm] = deriveDecoder[AlignmentClustalLoadHitsForm]

}
