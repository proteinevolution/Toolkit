package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor, Json }

case class AlignmentResult(alignment: List[AlignmentItem])

object AlignmentResult {

  implicit val alignmentResultDecoder: Decoder[AlignmentResult] = (c: HCursor) => {
    (for {
      alignment <- c.as[List[Json]]
    } yield {
      alignment
    }).map { alignment =>
        alignment.zipWithIndex.map {
          case (j, i) => AlignmentItem.alignmentItemDecoder(j, i)
        }
      }
      .map { items => new AlignmentResult(items.flatMap(_.right.toOption))
      }
  }

}
