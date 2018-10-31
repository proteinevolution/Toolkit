package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHPredQuery(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    ss_dssp: String,
    ss_pred: String,
    start: Int
)

object HHPredQuery {

  implicit val hhpredQueryDecoder: Decoder[HHPredQuery] = deriveDecoder

}
