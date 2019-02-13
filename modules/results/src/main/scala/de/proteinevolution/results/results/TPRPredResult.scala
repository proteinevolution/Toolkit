package de.proteinevolution.results.results

import de.proteinevolution.results.results.TPRPredResult._
import io.circe.{ Decoder, DecodingFailure, HCursor, Json }

case class TPRPredResult(info: List[Desc], hits: List[Hit])

object TPRPredResult {

  implicit def tprpredDecoder(jobId: String, json: Json): Either[DecodingFailure, TPRPredResult] =
    for {
      info <- json.hcursor.downField(jobId).downField("desc").as[List[Desc]]
      hits <- json.hcursor.downField(jobId).downField("hits").as[List[Hit]]
    } yield new TPRPredResult(info, hits)

  case class Desc(title: Option[String], value: Option[String])

  object Desc {
    implicit val descDecoder: Decoder[Desc] = (c: HCursor) =>
      for {
        title <- c.downArray.first.as[Option[String]]
        value <- c.downArray.right.as[Option[String]]
      } yield new Desc(title, value)
  }

  case class Hit(
      alignment: Option[String],
      repeat: Option[String],
      begin: Option[String],
      end: Option[String],
      pValue: Option[String]
  )

  object Hit {
    implicit val hitDecoder: Decoder[Hit] = (c: HCursor) =>
      for {
        alignment <- c.downArray.first.as[Option[String]]
        repeat    <- c.downArray.right.as[Option[String]]
        begin     <- c.downArray.rightN(2).as[Option[String]]
        end       <- c.downArray.rightN(3).as[Option[String]]
        pValue    <- c.downArray.rightN(4).as[Option[String]]
      } yield new Hit(alignment, repeat, begin, end, pValue)
  }

}
