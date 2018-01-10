package de.proteinevolution.tools.results

import javax.inject.Inject
import javax.inject.Singleton

import de.proteinevolution.tools.results.Alignment.AlignmentResult
import de.proteinevolution.tools.results.General.{ DTParam, SingleSeq }
import de.proteinevolution.tools.results.HHomp._
import play.api.libs.json._
@Singleton
class HHomp @Inject()(general: General) extends SearchTool[HHompHSP] {

  def parseResult(jsValue: JsValue): HHompResult = {
    val obj        = jsValue.as[JsObject]
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
    val query        = general.parseSingleSeq((obj \ "query").as[JsArray])
    val num_hits     = hsplist.length

    HHompResult(hsplist, num_hits, query, db, overall_prob)

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
}

object HHomp {
  case class HHompHSP(query: HHompQuery,
                      template: HHompTemplate,
                      info: HHompInfo,
                      agree: String,
                      description: String,
                      num: Int,
                      ss_score: Double,
                      length: Int,
                      evalue: Double = -1,
                      accession: String = "")
      extends HSP {
    def toDataTable(db: String = ""): JsValue = {
      val _ = db
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
  }
  case class HHompInfo(aligned_cols: Int,
                       evalue: Double,
                       identities: Double,
                       probab_hit: Double,
                       probab_omp: Double,
                       score: Double)
      extends SearchToolInfo
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
      extends HHTemplate
  case class HHompResult(HSPS: List[HHompHSP],
                         num_hits: Int,
                         query: SingleSeq,
                         db: String,
                         overall_prob: Double,
                         alignment: AlignmentResult = AlignmentResult(Nil))
      extends SearchResult[HHompHSP] {
    def hitsOrderBy(params: DTParam): List[HHompHSP] = {
      (params.iSortCol, params.sSortDir) match {
        case (1, "asc")  => HSPS.sortBy(_.template.accession)
        case (1, "desc") => HSPS.sortWith(_.template.accession > _.template.accession)
        case (2, "asc")  => HSPS.sortBy(_.description)
        case (2, "desc") => HSPS.sortWith(_.description > _.description)
        case (3, "asc")  => HSPS.sortBy(_.info.probab_hit)
        case (3, "desc") => HSPS.sortWith(_.info.probab_hit > _.info.probab_hit)
        case (4, "asc")  => HSPS.sortBy(_.info.probab_omp)
        case (4, "desc") => HSPS.sortWith(_.info.probab_omp > _.info.probab_omp)
        case (5, "asc")  => HSPS.sortBy(_.info.evalue)
        case (5, "desc") => HSPS.sortWith(_.info.evalue > _.info.evalue)
        case (6, "asc")  => HSPS.sortBy(_.ss_score)
        case (6, "desc") => HSPS.sortWith(_.ss_score > _.ss_score)
        case (7, "asc")  => HSPS.sortBy(_.info.aligned_cols)
        case (7, "desc") => HSPS.sortWith(_.info.aligned_cols > _.info.aligned_cols)
        case (8, "asc")  => HSPS.sortBy(_.template.ref)
        case (8, "desc") => HSPS.sortWith(_.template.ref > _.template.ref)
        case (_, "asc")  => HSPS.sortBy(_.num)
        case (_, "desc") => HSPS.sortWith(_.num > _.num)
        case (_, _)      => HSPS.sortBy(_.num)
      }
    }
  }
}
