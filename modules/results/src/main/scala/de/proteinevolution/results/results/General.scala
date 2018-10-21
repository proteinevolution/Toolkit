package de.proteinevolution.results.results

import io.circe._

import scala.util.matching.Regex

object General {

  private final val accessionMalFormat: Regex = """.*\|(.*)\|.*""".r

  def refineAccession(seq: String): String = seq match {
    case accessionMalFormat(group) => group
    case _                         => seq
  }

  case class DTParam(
      draw: Int,
      searchValue: String,
      displayStart: Int,
      pageLength: Int,
      orderCol: Int,
      orderDir: String
  )

  case class SingleSeq(accession: String, seq: String)

  object SingleSeq {

    implicit val decodeSingleSeq: Decoder[SingleSeq] = (c: HCursor) =>
      for {
        accession <- c.downArray.first.downArray.first.as[String]
        seq       <- c.downArray.first.downArray.right.as[String]
      } yield {
        new SingleSeq(accession, seq)
    }

  }

}
