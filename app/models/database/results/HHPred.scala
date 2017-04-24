package models.database.results

import javax.inject.Inject
import javax.inject.Singleton

import scala.concurrent.ExecutionContext.Implicits.global
import controllers.DTParam
import models.results.BlastVisualization
import play.api.libs.json._

import scala.concurrent.Future
/**
  * Created by drau on 18.04.17.
  */


case class HHPredHSP(query: HHPredQuery, template: HHPredTemplate, info: HHPredInfo, agree: String, description: String, num: Int){
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

case class HHPredInfo(aligned_cols: Int, evalue: Double, identities: Double, probab: Double, score: Double, similarity: Double)
case class HHPredQuery(consensus: String, end: Int, accession: String, ref: Int, seq: String, ss_pred: String, ss_dssp: String, start: Int)
case class HHPredTemplate(consensus: String, end: Int, accession: String, ref: Int, seq: String, ss_pred: String, ss_dssp: String, start: Int)
case class HHPredResult(HSPS: List[HHPredHSP], alignment : Alignment, num_hits: Int, query: Query, db: String, proteomes: String)

@Singleton
class HHPred @Inject() (general: General) {

  def parseResult(jsValue: JsValue): HHPredResult = jsValue match {
    case obj: JsObject => try {
      val jobID = (obj \ "jobID").as[String]
      val alignments = (obj \ jobID \ "alignments").as[List[JsObject]]
      val hits = (obj \ jobID \ "hits").as[List[JsObject]]
      val hsplist = alignments.zip(hits).map{ x =>
        val queryResult = parseQuery((x._1 \  "query").as[JsObject])
        val infoResult = parseInfo((x._1 \ "info").as[JsObject])
        val templateResult = parseTemplate(( x._1 \ "template").as[JsObject], x._2)
        val agree = (x._1 \ "agree").as[String]
        val description = ( x._1 \ "header").as[String]
        val num = (x._1 \ "no").getOrElse(Json.toJson(-1)).as[String].toInt
        HHPredHSP(queryResult, templateResult, infoResult, agree, description, num)
      }
      val db = (obj \ jobID \ "db").as[String]
      val proteomes = (obj \ jobID \ "proteomes").as[String]
      val alignment = general.parseAlignment((obj \ "reduced").as[JsArray])
      val query = general.parseQuery((obj \  "query").as[JsArray])
      val num_hits = hsplist.length

      HHPredResult(hsplist, alignment, num_hits, query, db, proteomes)
    }
  }

  def parseQuery(obj: JsObject): HHPredQuery= {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = (obj \ "struc").getOrElse(Json.toJson("")).as[String]
    val ref = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val ss_dssp = (obj \ "ss_dssp").getOrElse(Json.toJson("")).as[String]
    val ss_pred = (obj \ "ss_pred").getOrElse(Json.toJson("")).as[String]
    val start = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHPredQuery(consensus, end, accession, ref, seq, ss_dssp, ss_pred, start)
  }

  def parseInfo(obj: JsObject): HHPredInfo = {
    val aligned_cols = (obj \ "aligned_cols").getOrElse(Json.toJson(-1)).as[Int]
    val eval = (obj \ "eval").getOrElse(Json.toJson(-1)).as[Double]
    val identities = (obj \ "identities").getOrElse(Json.toJson(-1)).as[Double]
    val probab = (obj \ "probab").getOrElse(Json.toJson(-1)).as[Double]
    val score = (obj \ "score").getOrElse(Json.toJson(-1)).as[Double]
    val similarity = (obj \ "similarity").getOrElse(Json.toJson(-1)).as[Double]
    HHPredInfo(aligned_cols, eval, identities, probab, score, similarity)
  }

  def parseTemplate(obj: JsObject, hits: JsObject) : HHPredTemplate = {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = (obj \ "name").getOrElse(Json.toJson("")).as[String]
    val ref = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val ss_dssp = (obj \ "ss_dssp").getOrElse(Json.toJson("")).as[String]
    val ss_pred = (obj \ "ss_pred").getOrElse(Json.toJson("")).as[String]
    val start = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHPredTemplate(consensus, end, accession, ref, seq, ss_dssp, ss_pred, start)
  }


  def hitsOrderBy(params: DTParam, hits: Future[List[HHPredHSP]]) = {
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
