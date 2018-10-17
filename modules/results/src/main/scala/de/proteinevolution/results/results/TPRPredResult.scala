package de.proteinevolution.results.results

import io.circe.{ DecodingFailure, Json }

case class TPRPredResult(info: List[String], hits: List[String])

object TPRPredResult {

  implicit def tprpredDecoder(jobId: String, json: Json): Either[DecodingFailure, TPRPredResult] =
    for {
      info   <- json.hcursor.downField(jobId).downField("desc").focus
      hits   <- json.hcursor.downField(jobId).downField("hits").focus
      info_0 <- info.hcursor.downField("0").as[String]
      info_1 <- info.hcursor.downField("1").as[String]
      hits_0 <- hits.hcursor.downField("0").as[String]
      hits_1 <- hits.hcursor.downField("1").as[String]
      hits_2 <- hits.hcursor.downField("2").as[String]
      hits_3 <- hits.hcursor.downField("3").as[String]
      hits_4 <- hits.hcursor.downField("4").as[String]
    } yield {
      new TPRPredResult(List(info_0, info_1), List(hits_0, hits_1, hits_2, hits_3, hits_4))
    }

}
