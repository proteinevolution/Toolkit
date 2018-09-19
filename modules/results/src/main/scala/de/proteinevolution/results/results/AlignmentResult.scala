package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor, Json }

case class AlignmentResult(alignment: List[AlignmentItem])

object AlignmentResult {

  implicit val alignmentResultDecoder: Decoder[AlignmentResult] = (c: HCursor) => {
    for {
      alignment      <- c.downArray.as[List[Json]]
      (j, i)         <- alignment.zipWithIndex
      alignmentItems <- AlignmentItem.alignmentItemDecoder(j, i)
    } yield {
      alignmentItems
    }
  }

}
