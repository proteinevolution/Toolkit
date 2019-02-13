package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor, Json }

case class AlignmentResult(alignment: List[AlignmentItem])

object AlignmentResult {

  implicit val alignmentResultDecoder: Decoder[AlignmentResult] = (c: HCursor) => {
    c.as[List[Json]]
      .map(_.zipWithIndex.map {
        case (j, i) => AlignmentItem.alignmentItemDecoder(j, i)
      })
      .map(items => new AlignmentResult(items.flatMap(_.right.toOption)))
  }

}
