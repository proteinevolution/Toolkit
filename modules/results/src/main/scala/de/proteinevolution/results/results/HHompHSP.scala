package de.proteinevolution.results.results

import io.circe.syntax._
import io.circe._

case class HHompHSP(
    query: HHompQuery,
    template: HHompTemplate,
    info: HHompInfo,
    agree: String,
    description: String,
    num: Int,
    ss_score: Double,
    length: Int,
    eValue: Double = -1,
    accession: String = ""
) extends HSP {

  import SearchResultImplicits._

  def toDataTable(db: String = ""): Json = {
    val _ = db
    Map[String, Either[Either[Double, Int], String]](
      "0" -> Right(Common.getAddScrollLink(num)),
      "1" -> Right(template.accession),
      "2" -> Right(description.slice(0, 18)),
      "3" -> Left(Left(info.probabHit)),
      "4" -> Left(Left(info.probabOMP)),
      "5" -> Left(Left(info.eval)),
      "6" -> Left(Left(ss_score)),
      "7" -> Left(Right(info.alignedCols)),
      "8" -> Left(Right(template.ref))
    ).asJson
  }
}

object HHompHSP {

  implicit def hhompHSPDecoder(struct: String): Decoder[HHompHSP] =
    (c: HCursor) =>
      for {
        queryResult    <- c.downField("query").as[HHompQuery]
        infoResult     <- c.downField("info").as[HHompInfo]
        templateResult <- c.downField("template").as[HHompTemplate](HHompTemplate.hhompTemplateDecoder(struct))
        agree          <- c.downField("agree").as[String]
        description    <- c.downField("header").as[String]
        num            <- c.downField("no").as[Int]
        ss_score       <- c.downField("ss").as[Double]
      } yield {
        new HHompHSP(queryResult, templateResult, infoResult, agree, description, num, ss_score, agree.length)
    }

  def hhompHSPListDecoder(hits: List[Json], alignments: List[Json]): List[HHompHSP] = {
    alignments.zip(hits).flatMap {
      case (a, h) =>
        (for {
          struct <- h.hcursor.downField("struc").as[String]
          hsp    <- a.hcursor.as[HHompHSP](hhompHSPDecoder(struct))
        } yield {
          hsp
        }).toOption
    }
  }

}

