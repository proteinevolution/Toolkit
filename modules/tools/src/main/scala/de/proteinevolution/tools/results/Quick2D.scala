package de.proteinevolution.tools.results

import javax.inject.Inject
import javax.inject.Singleton

import de.proteinevolution.tools.results.General.SingleSeq
import de.proteinevolution.tools.results.Quick2D._
import play.api.libs.json.{ JsValue, JsObject, JsArray, Json }

@Singleton
class Quick2D @Inject()(general: General) {

  def parseResult(jsValue: JsValue): Quick2DResult = {
    val obj         = jsValue.as[JsObject]
    val jobID       = (obj \ "jobID").as[String]
    val query       = general.parseSingleSeq((obj \ "query").as[JsArray])
    val psipred     = parsePsipred((obj \ jobID).as[JsObject])
    val marcoil     = parseMarcoil((obj \ jobID).as[JsObject])
    val coils       = parseCoils((obj \ jobID).as[JsObject])
    val pcoils      = parsePcoils((obj \ jobID).as[JsObject])
    val tmhmm       = parseTmhmm((obj \ jobID).as[JsObject])
    val phobius     = parsePhobius((obj \ jobID).as[JsObject])
    val polyphobius = parsePolyphobius((obj \ jobID).as[JsObject])
    val spider2     = parseSpider2((obj \ jobID).as[JsObject])
    val spotd       = parseSpotd((obj \ jobID).as[JsObject])
    val iupred      = parseIupred((obj \ jobID).as[JsObject])
    val disopred3   = parseDisopred3((obj \ jobID).as[JsObject])
    val signal      = parseSignal((obj \ jobID).as[JsObject])
    val psspred     = parsePsspred((obj \ jobID).as[JsObject])
    val deepcnf     = parseDeepcnf((obj \ jobID).as[JsObject])

    Quick2DResult(jobID,
                  query,
                  psipred,
                  marcoil,
                  coils,
                  pcoils,
                  tmhmm,
                  phobius,
                  polyphobius,
                  spider2,
                  spotd,
                  iupred,
                  disopred3,
                  signal,
                  psspred,
                  deepcnf)

  }

  private def parsePsipred(obj: JsObject): Psipred = {
    val conf = (obj \ "psipred_conf").getOrElse(Json.toJson("")).as[String]
    val seq  = (obj \ "psipred").getOrElse(Json.toJson("")).as[String]
    Psipred("psipred", seq, conf)
  }

  private def parseMarcoil(obj: JsObject): Marcoil = {
    val seq = (obj \ "marcoil").getOrElse(Json.toJson("")).as[String]
    Marcoil("marcoil", seq)
  }

  private def parseCoils(obj: JsObject): Coils = {
    val seq = (obj \ "coils_w28").getOrElse(Json.toJson("")).as[String]
    Coils("coils", seq)
  }

  private def parsePcoils(obj: JsObject): Pcoils = {
    val seq = (obj \ "pcoils_w28").getOrElse(Json.toJson("")).as[String]
    Pcoils("pcoils", seq)
  }

  private def parseTmhmm(obj: JsObject): Tmhmm = {
    val seq = (obj \ "tmhmm").getOrElse(Json.toJson("")).as[String]
    Tmhmm("tmhmm", seq)
  }

  private def parsePhobius(obj: JsObject): Phobius = {
    val seq = (obj \ "phobius").getOrElse(Json.toJson("")).as[String]
    Phobius("phobius", seq)
  }

  private def parsePolyphobius(obj: JsObject): Polyphobius = {
    val seq = (obj \ "polyphobius").getOrElse(Json.toJson("")).as[String]
    Polyphobius("polyphobius", seq)
  }

  private def parseSpider2(obj: JsObject): Spider2 = {
    val seq = (obj \ "spider2").getOrElse(Json.toJson("")).as[String]
    Spider2("spider2", seq)
  }

  private def parseSpotd(obj: JsObject): Spotd = {
    val seq = (obj \ "spot-d").getOrElse(Json.toJson("")).as[String]
    Spotd("spotd", seq)
  }

  private def parseIupred(obj: JsObject): Iupred = {
    val seq = (obj \ "iupred").getOrElse(Json.toJson("")).as[String]
    Iupred("iupred", seq)
  }

  private def parseDisopred3(obj: JsObject): Disopred3 = {
    val seq = (obj \ "disopred3").getOrElse(Json.toJson("")).as[String]
    Disopred3("disopred3", seq)
  }

  private def parseSignal(obj: JsObject): Signal = {
    val seq = (obj \ "signal").getOrElse(Json.toJson("")).as[String]
    Signal("Signal", seq)
  }

  private def parsePsspred(obj: JsObject): Psspred = {
    val seq = (obj \ "psspred").getOrElse(Json.toJson("")).as[String]
    Psspred("psspred", seq)
  }

  private def parseDeepcnf(obj: JsObject): Deepcnf = {
    val seq = (obj \ "deepcnf").getOrElse(Json.toJson("")).as[String]
    Deepcnf("deepcnf", seq)
  }
}

object Quick2D {
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
                           iupred: Iupred,
                           disopred3: Disopred3,
                           signal: Signal,
                           psspred: Psspred,
                           deepcnf: Deepcnf)

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
  case class Disopred3(name: String, seq: String)
  case class Signal(name: String, seq: String)
  case class Psspred(name: String, seq: String)
  case class Deepcnf(name: String, seq: String)
}
