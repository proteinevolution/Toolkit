package de.proteinevolution.tools.results

import javax.inject.Inject
import javax.inject.Singleton

import de.proteinevolution.tools.results.Alignment.AlignmentResult
import de.proteinevolution.tools.results.General.{ DTParam, SingleSeq }
import de.proteinevolution.tools.results.Hmmer.{ HmmerHSP, HmmerResult }
import play.api.libs.json._

@Singleton
class Hmmer @Inject()(general: General, aln: Alignment) extends SearchTool[HmmerHSP] {

  def parseResult(jsValue: JsValue): HmmerResult = {
    val obj   = jsValue.as[JsObject]
    val jobID = (obj \ "jobID").as[String]
    val alignment = (obj \ "alignment").as[List[JsArray]].zipWithIndex.map {
      case (x, index) =>
        aln.parseWithIndex(x, index)
    }
    val db       = (obj \ jobID \ "db").as[String]
    val query    = general.parseSingleSeq((obj \ "query").as[JsArray])
    val hsps     = (obj \ jobID \ "hsps").as[List[JsObject]]
    val num_hits = hsps.length
    val hsplist  = hsps.map(parseHSP)
    val TMPRED = (obj \ jobID \ "TMPRED").asOpt[String] match {
      case Some(data) => data
      case None       => "0"
    }
    val COILPRED = (obj \ jobID \ "COILPRED").asOpt[String] match {
      case Some(data) => data
      case None       => "1"
    }
    HmmerResult(hsplist, num_hits, AlignmentResult(alignment), query, db, TMPRED, COILPRED)
  }

  def parseHSP(hsp: JsObject): HmmerHSP = {
    val evalue         = (hsp \ "evalue").getOrElse(Json.toJson(-1)).as[Double]
    val full_evalue    = (hsp \ "full_evalue").getOrElse(Json.toJson(-1)).as[Double]
    val num            = (hsp \ "num").getOrElse(Json.toJson(-1)).as[Int]
    val bitscore       = (hsp \ "bitscore").getOrElse(Json.toJson(-1)).as[Double]
    val hit_start      = (hsp \ "hit_start").getOrElse(Json.toJson(-1)).as[Int]
    val hit_end        = (hsp \ "hit_end").getOrElse(Json.toJson(-1)).as[Int]
    val hit_seq        = (hsp \ "hit_seq").getOrElse(Json.toJson("")).as[String].toUpperCase
    val query_seq      = (hsp \ "query_seq").getOrElse(Json.toJson("")).as[String].toUpperCase
    val query_start    = (hsp \ "query_start").getOrElse(Json.toJson(-1)).as[Int]
    val query_end      = (hsp \ "query_end").getOrElse(Json.toJson(-1)).as[Int]
    val query_id       = (hsp \ "query_id").getOrElse(Json.toJson("")).as[String]
    val hit_len        = (hsp \ "hit_len").getOrElse(Json.toJson(-1)).as[Int]
    val accession      = general.refineAccession((hsp \ "hit_id").getOrElse(Json.toJson("")).as[String])
    val midline        = (hsp \ "aln_ann" \ "PP").getOrElse(Json.toJson("")).as[String].toUpperCase
    val description    = (hsp \ "hit_description").getOrElse(Json.toJson("")).as[String]
    val domain_obs_num = (hsp \ "domain_obs_num").getOrElse(Json.toJson(-1)).as[Int]
    HmmerHSP(
      evalue,
      full_evalue,
      num,
      bitscore,
      hit_start,
      hit_end,
      hit_seq,
      query_seq,
      query_start,
      query_end,
      query_id,
      hit_len,
      accession,
      midline,
      description,
      domain_obs_num
    )
  }
}

object Hmmer {
  case class HmmerHSP(evalue: Double,
                      full_evalue: Double,
                      num: Int,
                      bitscore: Double,
                      hit_start: Int,
                      hit_end: Int,
                      hit_seq: String,
                      query_seq: String,
                      query_start: Int,
                      query_end: Int,
                      query_id: String,
                      hit_len: Int,
                      accession: String,
                      midline: String,
                      description: String,
                      domain_obs_num: Int,
                      info: HmmerInfo = HmmerInfo(-1, -1, -1, -1))
      extends HSP {

    def toDataTable(db: String): JsValue =
      Json.toJson(
        Map(
          "0" -> Json.toJson(Common.getCheckbox(num)),
          "1" -> Json.toJson(Common.getSingleLinkDB(db, accession).toString),
          "2" -> Json.toJson(Common.addBreak(description.slice(0, 84))),
          "3" -> Json.toJson(full_evalue),
          "4" -> Json.toJson(evalue),
          "5" -> Json.toJson(bitscore),
          "6" -> Json.toJson(hit_len)
        )
      )
  }

  case class HmmerInfo(db_num: Int, db_len: Int, hsp_len: Int, iter_num: Int, evalue: Double = -1)
      extends SearchToolInfo

  case class HmmerResult(HSPS: List[HmmerHSP],
                         num_hits: Int,
                         alignment: AlignmentResult,
                         query: SingleSeq,
                         db: String,
                         TMPRED: String,
                         COILPRED: String)
      extends SearchResult[HmmerHSP] {
    def hitsOrderBy(params: DTParam): List[HmmerHSP] = {
      (params.iSortCol, params.sSortDir) match {
        case (1, "asc")  => HSPS.sortBy(_.accession)
        case (1, "desc") => HSPS.sortWith(_.accession > _.accession)
        case (2, "asc")  => HSPS.sortBy(_.description)
        case (2, "desc") => HSPS.sortWith(_.description > _.description)
        case (3, "asc")  => HSPS.sortBy(_.full_evalue)
        case (3, "desc") => HSPS.sortWith(_.full_evalue > _.full_evalue)
        case (4, "asc")  => HSPS.sortBy(_.evalue)
        case (4, "desc") => HSPS.sortWith(_.evalue > _.evalue)
        case (5, "asc")  => HSPS.sortBy(_.bitscore)
        case (5, "desc") => HSPS.sortWith(_.bitscore > _.bitscore)
        case (6, "asc")  => HSPS.sortBy(_.hit_len)
        case (6, "desc") => HSPS.sortWith(_.hit_len > _.hit_len)
        case (_, "asc")  => HSPS.sortBy(_.num)
        case (_, "desc") => HSPS.sortWith(_.num > _.num)
        case (_, _)      => HSPS.sortBy(_.num)
      }
    }
  }
}
