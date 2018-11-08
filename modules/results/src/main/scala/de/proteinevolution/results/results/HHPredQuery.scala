package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHPredQuery(
    consensus: String,
    end: Int,
    name: String,
    ref: Int,
    seq: String,
    ss_dssp: Option[String],
    ss_pred: Option[String],
    start: Int
) {

  def accession: String = name

}

object HHPredQuery {

  implicit val hhpredQueryDecoder: Decoder[HHPredQuery] = deriveDecoder

}
