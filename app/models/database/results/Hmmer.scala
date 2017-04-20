package models.database.results

import models.tools.Alignment
import play.api.libs.json.Reads
import play.api.libs.json._
import reactivemongo.bson._

import scala.util.parsing.json.JSON
/**
  * Created by drau on 18.04.17.
  */


case class HmmerHSP(evalue: Double, num: Int,
                    bitscore: Double,
                    hit_start: Int,
                    hit_end: Int,
                    hit_seq: String,
                    query_seq: String,
                    query_start: Int,
                    query_end: Int,
                    query_id: String,
                    hit_len: Int,
                    accession: String, midline: String, description: String, dom_exp_num: Double, domain_obs_num: Int)

case class HmmerInfo(db_num: Int, db_len: Int, hsp_len: Int, iter_num: Int)

case class HmmerResult(HSPS: List[HmmerHSP], num_hits: Int, alignment: List[AlignmentItem])


object Hmmer {



  def parseHmmerResult(jsValue: JsValue): HmmerResult = jsValue match {
    case obj: JsObject => try {
      val jobID = (obj \ "jobID").as[String]
      val alignment = (obj \ "alignment").as[List[JsArray]].map{ x =>
        General.parseAlignmentItem(x)
      }

      val hsps = (obj \ jobID \ "hsps").as[List[JsObject]]
      val hits = (obj \ jobID \ "hits").as[List[JsObject]]
      val num_hits = hits.length

      val hsplist = hsps.zip(hits).map{ x =>
        parseHmmerHSP(x._1, x._2)
      }
      HmmerResult(hsplist, num_hits, alignment)
    }
  }

  def parseHmmerHSP(hsp: JsObject, hit: JsObject): HmmerHSP = {
      val evalue = (hsp \ "evalue").getOrElse(Json.toJson(-1)).as[Double]
      val num = (hsp \ "num").getOrElse(Json.toJson(-1)).as[Int]
      val bitscore = (hsp \ "bitscore").getOrElse(Json.toJson(-1)).as[Double]
      val hit_start =(hsp \ "hit_start").getOrElse(Json.toJson(-1)).as[Int]
      val hit_end = (hsp \ "hit_end").getOrElse(Json.toJson(-1)).as[Int]
      val hit_seq = (hsp \ "hit_seq").getOrElse(Json.toJson("")).as[String].toUpperCase
      val query_seq = (hsp \ "query_seq").getOrElse(Json.toJson("")).as[String].toUpperCase
      val query_start = (hsp \ "query_start").getOrElse(Json.toJson(-1)).as[Int]
      val query_end = (hsp \ "query_end").getOrElse(Json.toJson(-1)).as[Int]
      val query_id = (hsp \ "query_id").getOrElse(Json.toJson("")).as[String]
      val hit_len = (hsp \ "hit_len").getOrElse(Json.toJson(-1)).as[Int]
      val accession = (hsp \ "hit_id").getOrElse(Json.toJson("")).as[String]
      val midline = (hsp \ "aln_ann" \ "PP").getOrElse(Json.toJson("")).as[String].toUpperCase
      val description = (hsp \ "hit_description").getOrElse(Json.toJson("")).as[String]
      val dom_exp_num = (hit \ "dom_exp_num").getOrElse(Json.toJson(-1)).as[Double]
      val domain_obs_num = (hit \ "domain_obs_num").getOrElse(Json.toJson(-1)).as[Int]
        HmmerHSP(evalue, num, bitscore, hit_start, hit_end, hit_seq, query_seq, query_start, query_end, query_id, hit_len, accession, midline, description, dom_exp_num, domain_obs_num)

  }

}
