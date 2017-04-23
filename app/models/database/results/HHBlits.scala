package models.database.results

import javax.inject.Inject
import javax.inject.Singleton

import scala.concurrent.ExecutionContext.Implicits.global
import controllers.DTParam
import models.results.BlastVisualization
import play.api.libs.json._

import scala.concurrent.Future
/**
  * Created by drau on 23.04.17.
  */


case class HHBlitsHSP(query: HHBlitsQuery, template: HHBlitsTemplate, info: HHBlitsInfo, agree: String, description: String, num: Int){
  def toDataTable (db: String): JsValue = Json.toJson(
    Map(
      "0" -> Json.toJson(BlastVisualization.getCheckbox(num)),
      "1" -> Json.toJson(BlastVisualization.getSingleLink(template.accession).toString),
      "2" -> Json.toJson(description),
      "3" -> Json.toJson(info.probab),
      "4" -> Json.toJson(info.evalue),
      "5" -> Json.toJson(info.aligned_cols),
      "6" -> Json.toJson(template.ref)))
}

case class HHBlitsInfo(aligned_cols: Int, evalue: Double, identities: Double, probab: Double, score: Double, similarity: Double)
case class HHBlitsQuery(consensus: String, end: Int, accession: String, ref: Int, seq: String, start: Int)
case class HHBlitsTemplate(consensus: String, end: Int, accession: String, ref: Int, seq: String, start: Int)
case class HHBlitsResult(HSPS: List[HHBlitsHSP], alignment : Alignment, num_hits: Int, query: Query, db: String)

@Singleton
class HHBlits @Inject() (general: General) {

  def parseResult(jsValue: JsValue): HHBlitsResult = jsValue match {
    case obj: JsObject => try {
      val jobID = (obj \ "jobID").as[String]
      val alignments = (obj \ jobID \ "alignments").as[List[JsObject]]

      val hsplist = alignments.map{ hit =>
        val queryResult = parseQuery((hit \  "query").as[JsObject])
        val infoResult = parseInfo((hit \ "info").as[JsObject])
        val templateResult = parseTemplate(( hit \ "template").as[JsObject])
        val agree = (hit \ "agree").as[String]
        val description = ( hit \ "header").as[String]
        val num = (hit \ "no").getOrElse(Json.toJson(-1)).as[String].toInt
        HHBlitsHSP(queryResult, templateResult, infoResult, agree, description, num)
      }
      val db = (obj \ jobID \ "db").as[String]
      val alignment = general.parseAlignment((obj \ "rep100").as[JsArray])
      val query = general.parseQuery((obj \  "query").as[JsArray])
      val num_hits = hsplist.length

      HHBlitsResult(hsplist, alignment, num_hits, query, db)
    }
  }

  def parseQuery(obj: JsObject): HHBlitsQuery= {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = (obj \ "name").getOrElse(Json.toJson("")).as[String]
    val ref = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val start = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHBlitsQuery(consensus, end, accession, ref, seq, start)
  }

  def parseInfo(obj: JsObject): HHBlitsInfo = {
    val aligned_cols = (obj \ "aligned_cols").getOrElse(Json.toJson(-1)).as[Int]
    val eval = (obj \ "eval").getOrElse(Json.toJson(-1)).as[Double]
    val identities = (obj \ "identities").getOrElse(Json.toJson(-1)).as[Double]
    val probab = (obj \ "probab").getOrElse(Json.toJson(-1)).as[Double]
    val score = (obj \ "score").getOrElse(Json.toJson(-1)).as[Double]
    val similarity = (obj \ "similarity").getOrElse(Json.toJson(-1)).as[Double]
    HHBlitsInfo(aligned_cols, eval, identities, probab, score, similarity)
  }

  def parseTemplate(obj: JsObject) : HHBlitsTemplate = {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = (obj \ "name").getOrElse(Json.toJson("")).as[String]
    val ref = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val start = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHBlitsTemplate(consensus, end, accession, ref, seq, start)
  }


  def hitsOrderBy(params: DTParam, hits: Future[List[HHBlitsHSP]]) = {
    (params.iSortCol, params.sSortDir) match {
      case (1, "asc") => hits.map(x => x.sortBy(_.template.accession))
      case (1, "desc") => hits.map(x => x.sortWith(_.template.accession > _.template.accession))
      case (2, "asc") => hits.map(x => x.sortBy(_.description))
      case (2, "desc") => hits.map(x => x.sortWith(_.description > _.description))
      case (3, "asc") => hits.map(x => x.sortBy(_.info.probab))
      case (3, "desc") => hits.map(x => x.sortWith(_.info.probab > _.info.probab))
      case (4, "asc") => hits.map(x => x.sortBy(_.info.evalue))
      case (4, "desc") => hits.map(x => x.sortWith(_.info.evalue > _.info.evalue))
      case (5, "asc") => hits.map(x => x.sortBy(_.info.aligned_cols))
      case (5, "desc") => hits.map(x => x.sortWith(_.info.aligned_cols > _.info.aligned_cols))
      case (6, "asc") => hits.map(x => x.sortBy(_.template.ref))
      case (6, "desc") => hits.map(x => x.sortWith(_.template.ref > _.template.ref))
      case (_, _) => hits.map(x => x.sortBy(_.num))
    }
  }
}
