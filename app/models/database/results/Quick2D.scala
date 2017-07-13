package models.database.results

import javax.inject.Inject
import javax.inject.Singleton

import play.api.libs.json._


/**
  * Created by drau on 13.07.17.
  */



case class Quick2DResult(jobID: String,
                         query: Query,
                         psipred: Psipred
                        )

case class Psipred(name: String, seq: String, conf: String)
@Singleton
class Quick2D @Inject()(general: General) {

  def parseResult(jsValue: JsValue): Quick2DResult = jsValue match {
    case obj: JsObject =>
      val jobID      = (obj \ "jobID").as[String]
      val query = general.parseQuery((obj \ "query").as[JsArray])
      val psipred = parsePsipred((obj \ jobID).as[JsObject])
      Quick2DResult(jobID, query, psipred)

  }

  def parsePsipred(obj: JsObject) : Psipred = {
    val conf       = (obj \ "psipred_conf").getOrElse(Json.toJson("")).as[String]
    val seq        = (obj \ "psipred").getOrElse(Json.toJson("")).as[String]
    Psipred("psipred", seq, conf)
  }
}
