package models.database.results

import javax.inject.Inject
import javax.inject.Singleton

import play.api.libs.json._


/**
  * Created by drau on 13.07.17.
  */



case class Quick2DResult(jobID: String,
                         query: SingleSeq,
                         psipred: Psipred,
                         marcoil: Marcoil,
                         coils: Coils,
                         pcoils: Pcoils,
                         tmhmm: Tmhmm,
                         phobius: Phobius,
                         polyphobius: Polyphobius,
                         spider2: Spider2,
                         spotd: Spotd,
                         iupred: Iupred
                        )

case class Psipred(name: String, seq: String, conf: String)
case class Marcoil(name: String, seq: String)
case class Coils(name: String, seq: String)
case class Pcoils(name: String, seq: String)
case class Tmhmm(name: String, seq: String)
case class Phobius(name: String, seq: String)
case class Polyphobius(name: String, seq: String)
case class Spider2(name: String, seq: String)
case class Spotd(name: String, seq: String)
case class Iupred(name: String, seq: String)

@Singleton
class Quick2D @Inject()(general: General) {

  def parseResult(jsValue: JsValue): Quick2DResult = jsValue match {
    case obj: JsObject =>
      val jobID      = (obj \ "jobID").as[String]
      val query = general.parseSingleSeq((obj \ "query").as[JsArray])
      val psipred = parsePsipred((obj \ jobID).as[JsObject])
      val marcoil = parseMarcoil((obj \ jobID).as[JsObject])
      val coils = parseCoils((obj \ jobID).as[JsObject])
      val pcoils = parsePcoils((obj \ jobID).as[JsObject])
      val tmhmm = parseTmhmm((obj \ jobID).as[JsObject])
      val phobius = parsePhobius((obj \ jobID).as[JsObject])
      val polyphobius = parsePolyphobius((obj \ jobID).as[JsObject])
      val spider2 = parseSpider2((obj \ jobID).as[JsObject])
      val spotd = parseSpotd((obj \ jobID).as[JsObject])
      val iupred = parseIupred((obj \ jobID).as[JsObject])
      Quick2DResult(jobID, query, psipred, marcoil, coils, pcoils, tmhmm, phobius, polyphobius, spider2, spotd, iupred)

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

  def parsePhobius(obj: JsObject) : Phobius = {
    val seq        = (obj \ "phobius").getOrElse(Json.toJson("")).as[String]
    Phobius("phobius", seq)
  }

  def parsePolyphobius(obj: JsObject) : Polyphobius = {
    val seq        = (obj \ "jphobius").getOrElse(Json.toJson("")).as[String]
    Polyphobius("polyphobius", seq)
  }
  def parseSpider2(obj: JsObject) : Spider2 = {
    val seq        = (obj \ "spider2").getOrElse(Json.toJson("")).as[String]
    Spider2("spider2", seq)
  }
  def parseSpotd(obj: JsObject) : Spotd = {
    val seq        = (obj \ "spot-d").getOrElse(Json.toJson("")).as[String]
    Spotd("spotd", seq)
  }
  def parseIupred(obj: JsObject) : Iupred = {
    val seq        = (obj \ "iupred").getOrElse(Json.toJson("")).as[String]
    Iupred("iupred", seq)
  }

}
