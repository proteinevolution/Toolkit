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
) extends HHTemplate

object HHPredTemplate {
  implicit def hhpredTemplateDecoder(struct: String): Decoder[HHPredTemplate] =
    (c: HCursor) =>
      for {
        consensus <- c.downField("consensus").as[Option[String]]
        end       <- c.downField("end").as[Option[Int]]
        ref       <- c.downField("ref").as[Option[Int]]
        seq       <- c.downField("seq").as[Option[String]]
        ss_dssp   <- c.downField("ss_dssp").as[Option[String]]
        ss_pred   <- c.downField("ss_pred").as[Option[String]]
        start     <- c.downField("start").as[Option[Int]]
      } yield {
        val accession = General.refineAccession(struct)
        new HHPredTemplate(
          consensus.getOrElse(""),
          end.getOrElse(-1),
          accession,
          ref.getOrElse(-1),
          seq.getOrElse(""),
          ss_dssp.getOrElse(""),
          ss_pred.getOrElse(""),
          start.getOrElse(-1)
        )
    }
}
