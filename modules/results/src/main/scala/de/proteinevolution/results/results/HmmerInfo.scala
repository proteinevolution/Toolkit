package de.proteinevolution.results.results

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class HmmerInfo(
    db_num: Int,
    db_len: Int,
    hsp_len: Int,
    iter_num: Int,
    eval: Double = -1
) extends SearchToolInfo

object HmmerInfo {
  implicit val hhompInfoDecoder: Decoder[HmmerInfo] = deriveDecoder[HmmerInfo]
}
