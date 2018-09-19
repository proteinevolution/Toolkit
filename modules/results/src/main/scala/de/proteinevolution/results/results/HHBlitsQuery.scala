package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHBlitsQuery(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    start: Int
)

object HHBlitsQuery {

  implicit val hhblitsQueryDecoder: Decoder[HHBlitsQuery] = deriveDecoder[HHBlitsQuery]

}
