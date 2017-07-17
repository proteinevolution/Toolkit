package models.database.results

import javax.inject.Inject
import javax.inject.Singleton

import play.api.libs.json._


/**
  * Created by drau on 13.07.17.
  */



case class Quick2DResult(jobID: String,
                         query: Query,
                         psipred: Psipred,
                         marcoil: Marcoil,
                         coils: Coils,
                         pcoils: Pcoils,
                         tmhmm: Tmhmm
                        )

case class Psipred(name: String, seq: String, conf: String)
case class Marcoil(name: String, seq: String)
case class Coils(name: String, seq: String)
case class Pcoils(name: String, seq: String)
case class Tmhmm(name: String, seq: String)

@Singleton
class Quick2D @Inject()(general: General) {

  def parseResult(jsValue: JsValue): Quick2DResult = jsValue match {
    case obj: JsObject =>
      val jobID      = (obj \ "jobID").as[String]
      val query = general.parseQuery((obj \ "query").as[JsArray])
      val psipred = parsePsipred((obj \ jobID).as[JsObject])
      val marcoil = parseMarcoil((obj \ jobID).as[JsObject])
      val coils = parseCoils((obj \ jobID).as[JsObject])
      val pcoils = parsePcoils((obj \ jobID).as[JsObject])
      val tmhmm = parseTmhmm((obj \ jobID).as[JsObject])
      Quick2DResult(jobID, query, psipred, marcoil, coils, pcoils, tmhmm)

  }

  def parsePsipred(obj: JsObject) : Psipred = {
    val conf       = (obj \ "psipred_conf").getOrElse(Json.toJson("")).as[String]
    val seq        = (obj \ "psipred").getOrElse(Json.toJson("")).as[String]
    Psipred("psipred", seq, conf)
  }

  def parseMarcoil(obj: JsObject) : Marcoil = {
    val seq        = (obj \ "marcoil").getOrElse(Json.toJson("")).as[String]
    Marcoil("marcoil", seq)
  }

  def parseCoils(obj: JsObject) : Coils = {
    val seq        = (obj \ "coils_w28").getOrElse(Json.toJson("")).as[String]
    Coils("coils", seq)
  }

  def parsePcoils(obj: JsObject) : Pcoils = {
    val seq        = (obj \ "pcoils_w28").getOrElse(Json.toJson("")).as[String]
    Pcoils("pcoils", seq)
  }

  def parseTmhmm(obj: JsObject) : Tmhmm = {
    val seq        = (obj \ "tmhmm").getOrElse(Json.toJson("")).as[String]
    Tmhmm("tmhmm", seq)
  }


}
