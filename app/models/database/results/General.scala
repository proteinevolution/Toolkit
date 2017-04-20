package models.database.results
import play.api.libs.json._
/**
  * Created by drau on 20.04.17.
  */



case class AlignmentItem(accession: String, seq: String)
case class Alignment(alignment : List[AlignmentItem])

object General {

  def parseAlignment(jsValue: JsValue): Alignment = jsValue match {
    case obj: JsObject => try {
      val alignment = (obj \ "alignment").as[List[JsArray]]
      val list = alignment.map{ x =>
        parseAlignmentItem(x)
      }
      Alignment(list)
    }
  }

  def parseAlignmentItem(jsArray : JsArray): AlignmentItem = jsArray match{
    case arr: JsArray => try{
      val accession = (arr \ 0).as[String]
      val seq = (arr \ 1).as[String]
      AlignmentItem(accession, seq)
    }

  }
}
