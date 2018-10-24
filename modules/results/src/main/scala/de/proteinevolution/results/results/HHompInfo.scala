package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor }

case class HHompInfo(
    alignedCols: Int,
    eval: Double,
    identities: Double,
    probabHit: Double,
    probabOMP: Double,
    score: Double
) extends SearchToolInfo

object HHompInfo {

  implicit val hhompInfoDecoder: Decoder[HHompInfo] = (c: HCursor) =>
    for {
      alignedCols <- c.downField("aligned_cols").as[Int]
      eval        <- c.downField("eval").as[Double]
      identities  <- c.downField("identities").as[Double]
      probab_hit  <- c.downField("probab_hit").as[Double]
      probab_omp  <- c.downField("probab_OMP").as[Double]
      score       <- c.downField("score").as[Double]
    } yield new HHompInfo(alignedCols, eval, identities, probab_hit, probab_omp, score)

}
