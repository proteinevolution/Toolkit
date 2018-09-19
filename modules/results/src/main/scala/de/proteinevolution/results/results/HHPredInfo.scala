package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHPredInfo(
    alignedCols: Int,
    eval: Double,
    identities: Double,
    probab: Double,
    score: Double,
    similarity: Double
) extends SearchToolInfo

object HHPredInfo {
  implicit val hhpredInfoDecoder: Decoder[HHPredInfo] = deriveDecoder[HHPredInfo]
}
