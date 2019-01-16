package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor }

case class HHPredTemplate(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    ss_dssp: String,
    ss_pred: String,
    start: Int
)

object HHPredTemplate {

  implicit def hhpredTemplateDecoder(struct: String): Decoder[HHPredTemplate] =
    (c: HCursor) =>
      for {
        consensus <- c.downField("consensus").as[String]
        end       <- c.downField("end").as[Int]
        ref       <- c.downField("ref").as[Int]
        seq       <- c.downField("seq").as[String]
        ss_dssp   <- c.downField("ss_dssp").as[String]
        ss_pred   <- c.downField("ss_pred").as[String]
        start     <- c.downField("start").as[Int]
      } yield {
        val accession = General.refineAccession(struct)
        new HHPredTemplate(
          consensus,
          end,
          accession,
          ref,
          seq,
          ss_dssp,
          ss_pred,
          start
        )
    }

}
