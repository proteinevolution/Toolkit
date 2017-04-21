package models.database.results
import javax.inject.Singleton
import play.api.libs.json._
/**
  * Created by drau on 20.04.17.
  */



case class AlignmentItem(accession: String, seq: String)
case class Alignment(alignment : List[AlignmentItem])
case class Query(accession: String, seq: String)
@Singleton
class General {

  private val accessionMalFormat = """.*\|(.*)\|.*""".r

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

  def parseQuery(jsArray : JsArray): Query= jsArray match{
    case arr: JsArray => try{
      val accession = (arr \ 0 \ 0).get.as[String]
      val seq = (arr \ 0 \  1).get.as[String]
     Query(accession, seq)
    }
  }

  def refineAccession(seq: String) = seq match{
      case this.accessionMalFormat(group) => group
      case _ => seq
    }


}
