package de.proteinevolution.results.results

import io.circe.{ DecodingFailure, Json }

case class TPRPredResult(info: List[Option[String]], hits: List[Option[String]])

object TPRPredResult {

  implicit def tprpredDecoder(jobId: String, json: Json): Either[DecodingFailure, TPRPredResult] =
    for {
      info   <- json.hcursor.downField(jobId).downField("desc").as[Json]
      hits   <- json.hcursor.downField(jobId).downField("hits").as[Json]
      info_0 <- info.hcursor.downField("0").as[Option[String]]
      info_1 <- info.hcursor.downField("1").as[Option[String]]
      hits_0 <- hits.hcursor.downField("0").as[Option[String]]
      hits_1 <- hits.hcursor.downField("1").as[Option[String]]
      hits_2 <- hits.hcursor.downField("2").as[Option[String]]
      hits_3 <- hits.hcursor.downField("3").as[Option[String]]
      hits_4 <- hits.hcursor.downField("4").as[Option[String]]
    } yield {
      new TPRPredResult(List(info_0, info_1), List(hits_0, hits_1, hits_2, hits_3, hits_4))
    }

}
