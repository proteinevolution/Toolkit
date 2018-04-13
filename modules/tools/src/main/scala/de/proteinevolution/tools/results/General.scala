package de.proteinevolution.tools.results

import de.proteinevolution.tools.results.General.SingleSeq
import play.api.libs.json._

class General() {

  private val accessionMalFormat = """.*\|(.*)\|.*""".r

  def parseSingleSeq(arr: JsArray): SingleSeq = {
    val accession = (arr \ 0 \ 0).as[String]
    val seq       = (arr \ 0 \ 1).as[String]
    SingleSeq(accession, seq)
  }

  def refineAccession(seq: String): String = seq match {
    case this.accessionMalFormat(group) => group
    case _                              => seq
  }

}

object General {

  case class DTParam(draw: Int, searchValue: String, displayStart: Int, pageLength: Int, orderCol: Int, orderDir: String)
  case class SingleSeq(accession: String, seq: String)

}
