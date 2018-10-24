package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor }

case class HHompQuery(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    ss_conf: String,
    ss_dssp: String,
    ss_pred: String,
    start: Int
)

object HHompQuery {

  implicit val hhompQueryDecoder: Decoder[HHompQuery] = (c: HCursor) =>
    for {
      consensus <- c.downField("consensus").as[String]
      end       <- c.downField("end").as[Int]
      accession <- c.downField("name").as[String]
      ref       <- c.downField("ref").as[Int]
      seq       <- c.downField("seq").as[String]
      ss_conf   <- c.downField("ss_conf").as[String]
      ss_dssp   <- c.downField("ss_dssp").as[String]
      ss_pred   <- c.downField("ss_pred").as[String]
      start     <- c.downField("start").as[Int]
    } yield new HHompQuery(consensus, end, accession, ref, seq, ss_conf, ss_dssp, ss_pred, start)

}
