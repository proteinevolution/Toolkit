package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HHompQuery(
    consensus: String,
    end: Int,
    name: String,
    ref: Int,
    seq: String,
    ss_conf: String,
    ss_dssp: String,
    ss_pred: String,
    start: Int
) {

  def accession: String = name

}

object HHompQuery {

  implicit val hhompQueryDecoder: Decoder[HHompQuery] = deriveDecoder

}
