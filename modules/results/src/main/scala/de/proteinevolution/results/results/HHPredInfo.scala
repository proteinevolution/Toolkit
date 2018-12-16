package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHPredInfo(
    aligned_cols: Int,
    eval: Double,
    identities: Double,
    probab: Double,
    score: Double,
    similarity: Double
)

object HHPredInfo {

  implicit val hhpredInfoDecoder: Decoder[HHPredInfo] = deriveDecoder

}
