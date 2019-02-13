package de.proteinevolution.results.results

import io.circe.{ DecodingFailure, Json }

case class AlignmentItem(accession: String, seq: String, num: Int)

object AlignmentItem {

  implicit def alignmentItemDecoder(j: Json, i: Int): Either[DecodingFailure, AlignmentItem] =
    for {
      accession <- j.hcursor.downArray.first.as[String]
      seq       <- j.hcursor.downArray.right.as[String]
    } yield new AlignmentItem(accession, seq, i + 1)

}
