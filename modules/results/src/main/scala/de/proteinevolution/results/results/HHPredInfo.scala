package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor }

case class HHPredInfo(
    alignedCols: Int,
    eval: Double,
    identities: Double,
    probab: Double,
    score: Double,
    similarity: Double
) extends SearchToolInfo

object HHPredInfo {

  implicit val hhpredInfoDecoder: Decoder[HHPredInfo] = (c: HCursor) =>
    for {
      alignedCols <- c.downField("aligned_cols").as[Option[Int]]
      eval        <- c.downField("eval").as[Option[Double]]
      identities  <- c.downField("identities").as[Option[Double]]
      probab      <- c.downField("probab").as[Option[Double]]
      score       <- c.downField("score").as[Option[Double]]
      similarity  <- c.downField("similiarity").as[Option[Double]]
    } yield
      new HHPredInfo(
        alignedCols.getOrElse(-1),
        eval.getOrElse(-1D),
        identities.getOrElse(-1D),
        probab.getOrElse(-1D),
        score.getOrElse(-1D),
        similarity.getOrElse(-1D)
    )

}
