package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor }

case class HHBlitsTemplate(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    start: Int
) extends HHTemplate

object HHBlitsTemplate {

  implicit def hhblitsTemplateDecoder(struct: String): Decoder[HHBlitsTemplate] =
    (c: HCursor) =>
      for {
        consensus <- c.downField("consensus").as[String]
        end       <- c.downField("end").as[Int]
        ref       <- c.downField("ref").as[Int]
        seq       <- c.downField("seq").as[String]
        start     <- c.downField("start").as[Int]
      } yield {
        val accession = General.refineAccession(struct)
        new HHBlitsTemplate(consensus, end, accession, ref, seq, start)
    }

}
