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
case class HHompHSP(query: HHompQuery,
                    template: HHompTemplate,
                    info: HHompInfo,
                    agree: String,
                    description: String,
                    num: Int,
                    ss_score: Double,
                    length: Int) {
  def toDataTable(db: String): JsValue =
    Json.toJson(
      Map(
        "0" -> Json.toJson(Common.getAddScrollLink(num)),
        "1" -> Json.toJson(template.accession),
        "2" -> Json.toJson(description.slice(0, 18)),
        "3" -> Json.toJson(info.probab_hit),
        "4" -> Json.toJson(info.probab_omp),
        "5" -> Json.toJson(info.evalue),
        "6" -> Json.toJson(ss_score),
        "7" -> Json.toJson(info.aligned_cols),
        "8" -> Json.toJson(template.ref)
      )
    )
}
case class HHompInfo(aligned_cols: Int,
                     evalue: Double,
                     identities: Double,
                     probab_hit: Double,
                     probab_omp: Double,
                     score: Double)
case class HHompQuery(consensus: String,
                      end: Int,
                      accession: String,
                      ref: Int,
                      seq: String,
                      ss_conf: String,
                      ss_dssp: String,
                      ss_pred: String,
                      start: Int)
case class HHompTemplate(consensus: String,
                         end: Int,
                         accession: String,
                         ref: Int,
                         seq: String,
                         ss_conf: String,
                         ss_dssp: String,
                         ss_pred: String,
                         bb_pred: String,
                         bb_conf: String,
                         start: Int)
case class HHompResult(HSPS: List[HHompHSP],
                       alignment: AlignmentResult,
                       num_hits: Int,
                       query: SingleSeq,
                       db: String,
                       overall_prob: Double)

@Singleton
class HHomp @Inject()(general: General, aln: Alignment) {

  def parseResult(jsValue: JsValue): HHompResult = jsValue match {
    case obj: JsObject =>
      try {
        val jobID      = (obj \ "jobID").as[String]
        val alignments = (obj \ jobID \ "alignments").as[List[JsObject]]
        val hits       = (obj \ jobID \ "hits").as[List[JsObject]]
        val hsplist = alignments.zip(hits).map { x =>
          val queryResult    = parseQuery((x._1 \ "query").as[JsObject])
          val infoResult     = parseInfo((x._1 \ "info").as[JsObject])
          val templateResult = parseTemplate((x._1 \ "template").as[JsObject], x._2)
          val agree          = (x._1 \ "agree").as[String]
          val description    = (x._1 \ "header").as[String]
          val num            = (x._1 \ "no").getOrElse(Json.toJson(-1)).as[String].toInt
          val ss_score       = (x._2 \ "ss").getOrElse(Json.toJson(-1)).as[Double]
          HHompHSP(queryResult, templateResult, infoResult, agree, description, num, ss_score, agree.length)
        }
        val db           = (obj \ jobID \ "db").as[String]
        val overall_prob = (obj \ jobID \ "overallprob").as[Double]

        val alignment = aln.parseAlignment((obj \ "reduced").as[JsArray])
        val query     = general.parseSingleSeq((obj \ "query").as[JsArray])
        val num_hits  = hsplist.length

        HHompResult(hsplist, alignment, num_hits, query, db, overall_prob)
      } catch {

        case e: Exception => e.printStackTrace(); null
      }
  }

  def parseQuery(obj: JsObject): HHompQuery = {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end       = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = (obj \ "name").getOrElse(Json.toJson("")).as[String]
    val ref       = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq       = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val ss_conf   = (obj \ "ss_conf").getOrElse(Json.toJson("")).as[String]
    val ss_dssp   = (obj \ "ss_dssp").getOrElse(Json.toJson("")).as[String]
    val ss_pred   = (obj \ "ss_pred").getOrElse(Json.toJson("")).as[String]
    val start     = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHompQuery(consensus, end, accession, ref, seq, ss_conf, ss_dssp, ss_pred, start)
  }

  def parseInfo(obj: JsObject): HHompInfo = {
    val aligned_cols = (obj \ "aligned_cols").getOrElse(Json.toJson(-1)).as[Int]
    val eval         = (obj \ "eval").getOrElse(Json.toJson(-1)).as[Double]
    val identities   = (obj \ "identities").getOrElse(Json.toJson(-1)).as[Double]
    val probab_hit   = (obj \ "probab_hit").getOrElse(Json.toJson(-1)).as[Double]
    val probab_omp   = (obj \ "probab_OMP").getOrElse(Json.toJson(-1)).as[Double]
    val score        = (obj \ "score").getOrElse(Json.toJson(-1)).as[Double]
    val similarity   = (obj \ "similarity").getOrElse(Json.toJson(-1)).as[Double]
    HHompInfo(aligned_cols, eval, identities, probab_hit, probab_omp, score)

  }

  def parseTemplate(obj: JsObject, hits: JsObject): HHompTemplate = {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end       = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = general.refineAccession((hits \ "struc").getOrElse(Json.toJson("")).as[String])
    val ref       = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq       = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val ss_dssp   = (obj \ "ss_dssp").getOrElse(Json.toJson("")).as[String]
    val ss_pred   = (obj \ "ss_pred").getOrElse(Json.toJson("")).as[String]
    val start     = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    val ss_conf   = (obj \ "ss_conf").getOrElse(Json.toJson("")).as[String]
    val bb_pred   = (obj \ "bb_pred").getOrElse(Json.toJson("")).as[String]
    val bb_conf   = (obj \ "bb_conf").getOrElse(Json.toJson("")).as[String]
    HHompTemplate(consensus, end, accession, ref, seq, ss_conf, ss_dssp, ss_pred, bb_pred, bb_conf, start)
  }

  def hitsOrderBy(params: DTParam, hits: List[HHompHSP]): List[HHompHSP] = {
    (params.iSortCol, params.sSortDir) match {
      case (1, "asc")  => hits.sortBy(_.template.accession)
      case (1, "desc") => hits.sortWith(_.template.accession > _.template.accession)
      case (2, "asc")  => hits.sortBy(_.description)
      case (2, "desc") => hits.sortWith(_.description > _.description)
      case (3, "asc")  => hits.sortBy(_.info.probab_hit)
      case (3, "desc") => hits.sortWith(_.info.probab_hit > _.info.probab_hit)
      case (4, "asc")  => hits.sortBy(_.info.probab_omp)
      case (4, "desc") => hits.sortWith(_.info.probab_omp > _.info.probab_omp)
      case (5, "asc")  => hits.sortBy(_.info.evalue)
      case (5, "desc") => hits.sortWith(_.info.evalue > _.info.evalue)
      case (6, "asc")  => hits.sortBy(_.ss_score)
      case (6, "desc") => hits.sortWith(_.ss_score > _.ss_score)
      case (7, "asc")  => hits.sortBy(_.info.aligned_cols)
      case (7, "desc") => hits.sortWith(_.info.aligned_cols > _.info.aligned_cols)
      case (8, "asc")  => hits.sortBy(_.template.ref)
      case (8, "desc") => hits.sortWith(_.template.ref > _.template.ref)
      case (_, "asc")  => hits.sortBy(_.num)
      case (_, "desc") => hits.sortWith(_.num > _.num)
      case (_, _)      => hits.sortBy(_.num)
    }
  }
}
