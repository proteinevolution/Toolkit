package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor }

case class HHompTemplate(
    consensus: String,
    end: Int,
    accession: String,
    ref: Int,
    seq: String,
    ss_conf: String,
    ss_dssp: String,
    ss_pred: String,
    bb_pred: String,
    bb_conf: String,
    start: Int
) extends HHTemplate

object HHompTemplate {

  implicit def hhompTemplateDecoder(struct: String): Decoder[HHompTemplate] =
    (c: HCursor) =>
      for {
        consensus <- c.downField("consensus").as[String]
        end       <- c.downField("end").as[Int]
        ref       <- c.downField("ref").as[Int]
        seq       <- c.downField("seq").as[String]
        start     <- c.downField("start").as[Int]
        ss_dssp   <- c.downField("ss_dssp").as[String]
        ss_pred   <- c.downField("ss_pred").as[String]
        ss_conf   <- c.downField("ss_conf").as[String]
        bb_pred   <- c.downField("bb_pred").as[String]
        bb_conf   <- c.downField("bb_conf").as[String]
      } yield {
        val accession = General.refineAccession(struct)
        new HHompTemplate(consensus, end, accession, ref, seq, ss_conf, ss_dssp, ss_pred, bb_pred, bb_conf, start)
    }

}
