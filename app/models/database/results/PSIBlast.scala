package models.database.results

import play.api.libs.json._
/**
  * Created by drau on 18.04.17.
  */


case class PSIBlastHSP(evalue: Double, num: Int,
                       bitscore: Double,
                       score: Int,
                       hit_start: Int,
                       hit_end: Int,
                       hit_seq: String,
                       query_seq: String,
                       query_start: Int,
                       query_end: Int,
                       query_id: String,
                       hit_len: Int,
                       gaps: Int,
                       identity: Int,
                       positive: Int,
                       ref_len: Int,
                       accession: String, midline: String, description: String)

case class PSIBLastInfo(db_num: Int, db_len: Int, hsp_len: Int, iter_num: Int )

case class PSIBlastResult(HSPS: List[PSIBlastHSP], num_hits: Int, iter_num: Int, db: String, evalue: Double, alignment: List[AlignmentItem])


object PSIBlast {

  def parsePSIBlastResult(json: JsValue): PSIBlastResult = json match {
    case obj: JsObject => try {
      val jobID = (obj \ "jobID").as[String]
      val alignment = (obj \ "alignment").as[List[JsArray]].map{ x =>
        General.parseAlignmentItem(x)
      }
      val iter_num = (obj \ "output_psiblastp" \ "BlastOutput2" \ 0 \ "report" \ "results" \ "iterations" ).as[List[JsObject]].size-1
      val db = (obj \ "output_psiblastp" \ "db").as[String]
      val evalue = (obj \ "output_psiblastp" \ "evalue").as[String].toDouble
      val hits = (obj \ "output_psiblastp" \ "BlastOutput2" \ 0 \ "report" \ "results" \ "iterations" \ iter_num \ "search" \ "hits").as[List[JsObject]]
      val num_hits = hits.length
      val hsplist = hits.map{ x =>
        parsePSIBlastHSP(x)
      }
      PSIBlastResult(hsplist, num_hits, iter_num, db, evalue, alignment)
    }
  }

  def parsePSIBlastHSP(hit: JsObject): PSIBlastHSP = {
    val descriptionBase = hit \ "description" \ 0
    val hsps = hit \ "hsps" \ 0
    val evalue = (hsps \ "evalue").getOrElse(Json.toJson(-1)).as[Double]
    val num = (hit \ "num").getOrElse(Json.toJson(-1)).as[Int]
    val bitscore = (hsps \ "bit_score").getOrElse(Json.toJson(-1)).as[Double]
    val score = (hsps \ "score").getOrElse(Json.toJson(-1)).as[Int]
    val positive = (hsps \ "positive").getOrElse(Json.toJson(-1)).as[Int]
    val identity = (hsps \ "identity").getOrElse(Json.toJson(-1)).as[Int]
    val gaps = (hsps \ "gaps").getOrElse(Json.toJson(-1)).as[Int]
    val hit_start =(hsps \ "hit_from").getOrElse(Json.toJson(-1)).as[Int]
    val hit_end = (hsps \ "hit_to").getOrElse(Json.toJson(-1)).as[Int]
    val hit_seq = (hsps \ "hseq").getOrElse(Json.toJson("")).as[String].toUpperCase
    val query_seq = (hsps \ "qseq").getOrElse(Json.toJson("")).as[String].toUpperCase
    val query_start = (hsps \ "query_from").getOrElse(Json.toJson(-1)).as[Int]
    val query_end = (hsps \ "query_to").getOrElse(Json.toJson(-1)).as[Int]
    val query_id = (hsps \ "query_id").getOrElse(Json.toJson("")).as[String]
    val ref_len = (hit \ "ref_len").getOrElse(Json.toJson(-1)).as[Int]
    val hit_len = (hsps \ "align_len").getOrElse(Json.toJson(-1)).as[Int]
    val accession = (descriptionBase \ "accession").getOrElse(Json.toJson("")).as[String]
    val midline = (hsps \ "midline").getOrElse(Json.toJson("")).as[String].toUpperCase
    val description = (descriptionBase \ "title").getOrElse(Json.toJson("")).as[String]
    PSIBlastHSP(evalue, num, bitscore, score,  hit_start, hit_end, hit_seq, query_seq, query_start, query_end, query_id, hit_len, gaps, identity, positive, ref_len ,accession, midline, description)
  }



}

