package models.database.results

import javax.inject.Inject
import javax.inject.Singleton

import scala.concurrent.ExecutionContext.Implicits.global
import controllers.DTParam
import models.results.Common
import play.api.libs.json._

import scala.concurrent.Future

/**
  * Created by drau on 18.04.17.
  */
case class PSIBlastHSP(evalue: Double,
                       num: Int,
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
                       accession: String,
                       midline: String,
                       description: String) {
  def toDataTable(db: String): JsValue =
    Json.toJson(
      Map(
        "0" -> Json.toJson(Common.getCheckbox(num)),
        "1" -> Json.toJson(Common.getSingleLinkDB(db, accession).toString),
        "2" -> Json.toJson(Common.addBreak(description.slice(0, 84))),
        "3" -> Json.toJson("%.2e".format(evalue)),
        "4" -> Json.toJson(bitscore),
        "5" -> Json.toJson(ref_len),
        "6" -> Json.toJson(hit_len)
      )
    )
}

case class PSIBLastInfo(db_num: Int, db_len: Int, hsp_len: Int, iter_num: Int)

case class PSIBlastResult(HSPS: List[PSIBlastHSP],
                          num_hits: Int,
                          iter_num: Int,
                          db: String,
                          evalue: Double,
                          alignment: List[AlignmentItem],
                          query: SingleSeq,
                          belowEvalThreshold: Int,
                          TMPRED: String,
                          COILPRED: String)

@Singleton
class PSIBlast @Inject()(general: General, aln: Alignment) {

  def parseResult(json: JsValue): PSIBlastResult = json match {
    case obj: JsObject =>
      try {
        var belowEvalThreshold = -1;
        val jobID              = (obj \ "jobID").as[String]
        val alignment = (obj \ "alignment").as[List[JsArray]].zipWithIndex.map {
          case (x, index) =>
            aln.parseAlignmentItem(x, index)
        }
        val query = general.parseSingleSeq((obj \ "query").as[JsArray])
        val iter_num = (obj \ "output_psiblastp" \ "BlastOutput2" \ 0 \ "report" \ "results" \ "iterations")
          .as[List[JsObject]]
          .size - 1
        val db     = (obj \ "output_psiblastp" \ "db").as[String]
        val evalue = (obj \ "output_psiblastp" \ "evalue").as[String].toDouble
        val hits =
          (obj \ "output_psiblastp" \ "BlastOutput2" \ 0 \ "report" \ "results" \ "iterations" \ iter_num \ "search" \ "hits")
            .as[List[JsObject]]
        val num_hits = hits.length
        val hsplist = hits.map { hit =>
          // get num of last checkboxes that is checked by default
          if (belowEvalThreshold == -1 && (hit \ "hsps" \ 0 \ "evalue").as[Double] >= evalue) {
            belowEvalThreshold = (hit \ "num").as[Int]
          }
          parseHSP(hit, db, evalue)
        }
        // if all hits are below threshold
        // set belowEvalThreshold to total number of found hits
        if (belowEvalThreshold == -1) {
          belowEvalThreshold = hsplist.length + 1
        }
        val TMPRED = (obj \ "output_psiblastp" \ "TMPRED").asOpt[String] match {
          case Some(data) => data
          case None       => "0"
        }
        val COILPRED = (obj \ "output_psiblastp" \ "COILPRED").asOpt[String] match {
          case Some(data) => data
          case None       => "1"
        }
        PSIBlastResult(hsplist, num_hits, iter_num, db, evalue, alignment, query, belowEvalThreshold, TMPRED, COILPRED)
      }
  }

  def parseHSP(hit: JsObject, db: String, eval_threshold: Double): PSIBlastHSP = {
    val descriptionBase = hit \ "description" \ 0
    val hsps            = hit \ "hsps" \ 0
    val evalue          = (hsps \ "evalue").getOrElse(Json.toJson(-1)).as[Double]
    val num             = (hit \ "num").getOrElse(Json.toJson(-1)).as[Int]
    val bitscore        = (hsps \ "bit_score").getOrElse(Json.toJson(-1)).as[Double]
    val score           = (hsps \ "score").getOrElse(Json.toJson(-1)).as[Int]
    val positive        = (hsps \ "positive").getOrElse(Json.toJson(-1)).as[Int]
    val identity        = (hsps \ "identity").getOrElse(Json.toJson(-1)).as[Int]
    val gaps            = (hsps \ "gaps").getOrElse(Json.toJson(-1)).as[Int]
    val hit_start       = (hsps \ "hit_from").getOrElse(Json.toJson(-1)).as[Int]
    val hit_end         = (hsps \ "hit_to").getOrElse(Json.toJson(-1)).as[Int]
    val hit_seq         = (hsps \ "hseq").getOrElse(Json.toJson("")).as[String].toUpperCase
    val query_seq       = (hsps \ "qseq").getOrElse(Json.toJson("")).as[String].toUpperCase
    val query_start     = (hsps \ "query_from").getOrElse(Json.toJson(-1)).as[Int]
    val query_end       = (hsps \ "query_to").getOrElse(Json.toJson(-1)).as[Int]
    val query_id        = (hsps \ "query_id").getOrElse(Json.toJson("")).as[String]
    val ref_len         = (hit \ "len").getOrElse(Json.toJson(-1)).as[Int]
    val hit_len         = (hsps \ "align_len").getOrElse(Json.toJson(-1)).as[Int]
    var accession       = ""
    // workaround: bug of psiblast output when searching pdb_nr
    if (db == "pdb_nr") {
      accession = (descriptionBase \ "title").getOrElse(Json.toJson("")).as[String].split("\\s+").head
    } else {
      //accession = (descriptionBase \ "title").getOrElse(Json.toJson("")).as[String]
      accession = general.refineAccession((descriptionBase \ "accession").getOrElse(Json.toJson("")).as[String])
    }
    val midline     = (hsps \ "midline").getOrElse(Json.toJson("")).as[String].toUpperCase
    val description = (descriptionBase \ "title").getOrElse(Json.toJson("")).as[String]

    PSIBlastHSP(
      evalue,
      num,
      bitscore,
      score,
      hit_start,
      hit_end,
      hit_seq,
      query_seq,
      query_start,
      query_end,
      query_id,
      hit_len,
      gaps,
      identity,
      positive,
      ref_len,
      accession,
      midline,
      description
    )

  }

  def hitsOrderBy(params: DTParam, hits: List[PSIBlastHSP]): List[PSIBlastHSP] = {
    (params.iSortCol, params.sSortDir) match {
      case (1, "asc")  => hits.sortBy(_.accession)
      case (1, "desc") => hits.sortWith(_.accession > _.accession)
      case (2, "asc")  => hits.sortBy(_.description)
      case (2, "desc") => hits.sortWith(_.description > _.description)
      case (3, "asc")  => hits.sortBy(_.evalue)
      case (3, "desc") => hits.sortWith(_.evalue > _.evalue)
      case (4, "asc")  => hits.sortBy(_.bitscore)
      case (4, "desc") => hits.sortWith(_.bitscore > _.bitscore)
      case (5, "asc")  => hits.sortBy(_.ref_len)
      case (5, "desc") => hits.sortWith(_.ref_len > _.ref_len)
      case (6, "asc")  => hits.sortBy(_.hit_len)
      case (6, "desc") => hits.sortWith(_.hit_len > _.hit_len)
      case (_, "asc")  => hits.sortBy(_.num)
      case (_, "desc") => hits.sortWith(_.num > _.num)
      case (_, _)      => hits.sortBy(_.num)
    }
  }
}
