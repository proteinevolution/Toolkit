package de.proteinevolution.results.results

import de.proteinevolution.results.results.Alignment.{ AlignmentItem, AlignmentResult }
import javax.inject.Singleton
import play.api.libs.json.JsArray

@Singleton
class Alignment {

  def parse(obj: JsArray): AlignmentResult = {
    val alignment = obj.as[List[JsArray]]
    val list = alignment.zipWithIndex.map {
      case (data, index) =>
        parseWithIndex(data, index.toInt + 1)
    }
    AlignmentResult(list)
  }
  def parseWithIndex(jsArray: JsArray, index: Int): AlignmentItem = jsArray match {
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
