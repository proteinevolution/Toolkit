package de.proteinevolution.results.results

import io.circe.syntax._
import io.circe._

case class HHBlitsHSP(
    query: HHBlitsQuery,
    template: HHBlitsTemplate,
    info: HHBlitsInfo,
    agree: String,
    description: String,
    num: Int,
    length: Int
) extends HSP {

  import SearchResultImplicits._

  def toDataTable(db: String = ""): Json = {
    val _ = db
    Map[String, Either[Either[Double, Int], String]](
      "0" -> Right(Common.getCheckbox(num)),
      "1" -> Right(Common.getSingleLinkHHBlits(template.accession).toString),
      "2" -> Right(Common.addBreak(description)),
      "3" -> Left(Left(info.probab)),
      "4" -> Left(Left(info.eval)),
      "5" -> Left(Right(info.aligned_cols)),
      "6" -> Left(Right(template.ref))
    ).asJson
  }

  override val eValue: Double = info.eval

  override val accession: String = template.accession

}

object HHBlitsHSP {

  implicit def hhblitsHSPDecoder(struct: String): Decoder[HHBlitsHSP] =
    (c: HCursor) =>
      for {
        queryResult    <- c.downField("query").as[HHBlitsQuery]
        infoResult     <- c.downField("info").as[HHBlitsInfo]
        templateResult <- c.downField("template").as[HHBlitsTemplate](HHBlitsTemplate.hhblitsTemplateDecoder(struct))
        agree          <- c.downField("agree").as[String]
        description    <- c.downField("header").as[String]
        num            <- c.downField("no").as[Int]
      } yield
        new HHBlitsHSP(
          queryResult,
          templateResult,
          infoResult,
          agree,
          description,
          num,
          agree.length
      )

  def hhblitsHSPListDecoder(hits: List[Json], alignments: List[Json]): List[HHBlitsHSP] = {
    alignments.zip(hits).flatMap {
      case (a, h) =>
        (for {
          struct <- h.hcursor.downField("struc").as[String]
          hsp    <- a.hcursor.as[HHBlitsHSP](hhblitsHSPDecoder(struct))
        } yield {
          hsp
        }).toOption
    }
  }

}
