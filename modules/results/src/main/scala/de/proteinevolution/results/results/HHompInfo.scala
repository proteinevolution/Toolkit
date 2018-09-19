package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHompInfo(
    alignedCols: Int,
    eval: Double,
    identities: Double,
    probabHit: Double,
    probabOMP: Double,
    score: Double
) extends SearchToolInfo

object HHompInfo {
  implicit val hhompInfoDecoder: Decoder[HHompInfo] = deriveDecoder[HHompInfo]
}
