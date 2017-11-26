package de.proteinevolution.models.database.results

import javax.inject.Singleton

import de.proteinevolution.models.database.results.Alignment.{ AlignmentItem, AlignmentResult }
import play.api.libs.json.JsArray

@Singleton
class Alignment {

  def parseAlignment(obj: JsArray): AlignmentResult = {
    val alignment = obj.as[List[JsArray]]
    val list = alignment.zipWithIndex.map {
      case (data, index) =>
        parseAlignmentItem(data, index.toInt + 1)
    }
    AlignmentResult(list)
  }
  def parseAlignmentItem(jsArray: JsArray, index: Int): AlignmentItem = jsArray match {
    case arr: JsArray =>
      val accession = (arr \ 0).as[String]
      val seq       = (arr \ 1).as[String]
      AlignmentItem(accession, seq, index)
  }
}

object Alignment {

  case class AlignmentItem(accession: String, seq: String, num: Int)
  case class AlignmentResult(alignment: List[AlignmentItem])

}
