package models.database.results
import play.api.libs.json._
/**
  * Created by drau on 20.04.17.
  */



case class AlignmentItem(accession: String, seq: String)
case class Alignment(alignment : List[AlignmentItem])
case class Query(accession: String, seq: String)

object General {

  def parseAlignment(jsArray: JsArray): Alignment = jsArray match {
    case obj: JsArray => try {
      var alignment = obj.as[List[JsArray]]
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

  def parseQuery(jsArray : JsArray): Query = jsArray match{
    case arr: JsArray => try{
      val accession = (arr \ 0).as[String]
      val seq = (arr \ 1).as[String]
     Query(accession, seq)
    }
  }

}
