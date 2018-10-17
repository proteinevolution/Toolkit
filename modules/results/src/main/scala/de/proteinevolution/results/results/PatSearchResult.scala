package de.proteinevolution.results.results

import de.proteinevolution.results.results.PatSearchResult.Hit
import io.circe._, io.circe.generic.semiauto._

case class PatSearchResult(hits: List[Hit], len: Int)

object PatSearchResult {

  case class Hit(name: Option[String], seq: Option[String], pats: Option[List[Int]])

  object Hit {
    implicit val hitDecoder: Decoder[Hit] = deriveDecoder[Hit]
  }

  implicit def patSearchResultDecoder(json: Json, jobId: String): Either[DecodingFailure, PatSearchResult] =
    for {
      hitList <- json.hcursor.downArray.as[List[Hit]]
      len     <- json.hcursor.downField(jobId).downField("len").as[Int]
    } yield {
      new PatSearchResult(hitList, len)
    }

}
