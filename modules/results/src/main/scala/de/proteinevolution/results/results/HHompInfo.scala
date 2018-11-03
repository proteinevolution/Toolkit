package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHompInfo(
    aligned_cols: Int,
    eval: Double,
    identities: Double,
    probab_hit: Double,
    probab_OMP: Double,
    score: Double
) extends SearchToolInfo

object HHompInfo {

  implicit val hhompInfoDecoder: Decoder[HHompInfo] = deriveDecoder

}
