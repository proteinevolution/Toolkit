package de.proteinevolution.results.results

import io.circe.syntax._
import io.circe.{ Decoder, HCursor, Json }

case class HHPredHSP(
    query: HHPredQuery,
    template: HHPredTemplate,
    info: HHPredInfo,
    agree: String,
    description: String,
    num: Int,
    ss_score: Double,
    confidence: String,
    length: Int,
    eValue: Double = -1,
    accession: String = ""
) extends HSP {
  def toDataTable(db: String = ""): Json = {
    import SearchResultImplicits._
    val _ = db
    Map[String, Either[Either[Double, Int], String]](
      "0" -> Right(Common.getCheckbox(num)),
      "1" -> Right(Common.getSingleLink(template.accession).toString),
      "2" -> Right(Common.addBreakHHpred(description)),
      "3" -> Left(Left(info.probab)),
      "4" -> Left(Left(info.eval)),
      "5" -> Left(Left(ss_score)),
      "6" -> Left(Right(info.aligned_cols)),
      "7" -> Left(Right(template.ref))
    ).asJson
  }
}

object HHPredHSP {

  implicit def hhpredHSPDecoder(struct: String, ss_score: Double): Decoder[HHPredHSP] =
    (c: HCursor) =>
      for {
        queryResult    <- c.downField("query").as[HHPredQuery]
        infoResult     <- c.downField("info").as[HHPredInfo]
        templateResult <- c.downField("template").as[HHPredTemplate](HHPredTemplate.hhpredTemplateDecoder(struct))
        agree          <- c.downField("agree").as[String]
        description    <- c.downField("header").as[String]
        num            <- c.downField("no").as[Int]
        confidence     <- c.downField("confidence").as[String]
      } yield {
        new HHPredHSP(
          queryResult,
          templateResult,
          infoResult,
          agree,
          description,
          num,
          ss_score,
          confidence,
          agree.length
        )
    }

  def hhpredHSPListDecoder(hits: List[Json], alignments: List[Json]): List[HHPredHSP] = {
    alignments.zip(hits).flatMap {
      case (a, h) =>
        (for {
          struct   <- h.hcursor.downField("struc").as[Option[String]]
          ss_score <- h.hcursor.downField("ss").as[Option[Double]]
          hsp      <- a.hcursor.as[HHPredHSP](hhpredHSPDecoder(struct.getOrElse(""), ss_score.getOrElse(-1D)))
        } yield {
          hsp
        }).toOption
    }
  }

}
