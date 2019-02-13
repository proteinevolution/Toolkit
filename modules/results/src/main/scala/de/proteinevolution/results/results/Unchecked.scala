package de.proteinevolution.results.results

import io.circe.{ Decoder, HCursor }

case class Unchecked(ids: List[String])

object Unchecked {

  implicit val uncheckedDecoder: Decoder[Unchecked] = (c: HCursor) =>
    for {
      ids <- c.downField("ids").downField("ACC_IDS").as[List[String]]
    } yield new Unchecked(ids)

}
