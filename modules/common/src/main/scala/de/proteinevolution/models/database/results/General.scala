package de.proteinevolution.models.database.results

import de.proteinevolution.models.database.results.General.SingleSeq
import play.api.libs.json._

class General() {

  private val accessionMalFormat = """.*\|(.*)\|.*""".r

  def parseSingleSeq(jsArray: JsArray): SingleSeq = jsArray match {
    case arr: JsArray =>
      val accession = (arr \ 0 \ 0).get.as[String]
      val seq       = (arr \ 0 \ 1).get.as[String]
      SingleSeq(accession, seq)
  }

  def refineAccession(seq: String): String = seq match {
    case this.accessionMalFormat(group) => group
    case _                              => seq
  }

}

object General {

  case class DTParam(sSearch: String, iDisplayStart: Int, iDisplayLength: Int, iSortCol: Int, sSortDir: String)
  case class SingleSeq(accession: String, seq: String)

}
