package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor }

case class HHBlitsQuery(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    start: Int
)

object HHBlitsQuery {

  implicit val hhblitsQueryDecoder: Decoder[HHBlitsQuery] = (c: HCursor) =>
    for {
      consensus <- c.downField("consensus").as[String]
      end       <- c.downField("end").as[Int]
      accession <- c.downField("name").as[String]
      ref       <- c.downField("ref").as[Int]
      seq       <- c.downField("seq").as[String]
      start     <- c.downField("start").as[Int]
    } yield new HHBlitsQuery(consensus, end, accession, ref, seq, start)

}
