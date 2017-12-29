package de.proteinevolution.tools.results

import javax.inject.Inject
import javax.inject.Singleton

import de.proteinevolution.tools.results.Alignment.AlignmentResult
import de.proteinevolution.tools.results.General.{ DTParam, SingleSeq }
import de.proteinevolution.tools.results.HHBlits._
import play.api.libs.json._

@Singleton
class HHBlits @Inject()(general: General, aln: Alignment) extends SearchTool[HHBlitsHSP] {

  def parseResult(jsValue: JsValue): HHBlitsResult = {
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
      HHBlitsHSP(queryResult, templateResult, infoResult, agree, description, num, agree.length)
    }
    val db        = (obj \ jobID \ "db").as[String]
    val alignment = aln.parse((obj \ "reduced").as[JsArray])
    val query     = general.parseSingleSeq((obj \ "query").as[JsArray])
    val num_hits  = hsplist.length

    val TMPRED = (obj \ jobID \ "TMPRED").asOpt[String] match {
      case Some(data) => data
      case None       => "0"
    }
    val COILPRED = (obj \ jobID \ "COILPRED").asOpt[String] match {
      case Some(data) => data
      case None       => "1"
    }

    HHBlitsResult(hsplist, alignment, num_hits, query, db, TMPRED, COILPRED)

  }

  def parseQuery(obj: JsObject): HHBlitsQuery = {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end       = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = (obj \ "name").getOrElse(Json.toJson("")).as[String]
    val ref       = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq       = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val start     = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHBlitsQuery(consensus, end, accession, ref, seq, start)
  }

  def parseInfo(obj: JsObject): HHBlitsInfo = {
    val aligned_cols = (obj \ "aligned_cols").getOrElse(Json.toJson(-1)).as[Int]
    val eval         = (obj \ "eval").getOrElse(Json.toJson(-1)).as[Double]
    val identities   = (obj \ "identities").getOrElse(Json.toJson(-1)).as[Double]
    val probab       = (obj \ "probab").getOrElse(Json.toJson(-1)).as[Double]
    val score        = (obj \ "score").getOrElse(Json.toJson(-1)).as[Double]
    val similarity   = (obj \ "similarity").getOrElse(Json.toJson(-1)).as[Double]
    HHBlitsInfo(aligned_cols, eval, identities, probab, score, similarity)
  }

  def parseTemplate(obj: JsObject, hit: JsObject): HHBlitsTemplate = {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end       = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = general.refineAccession((hit \ "struc").getOrElse(Json.toJson("")).as[String])
    val ref       = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq       = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val start     = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHBlitsTemplate(consensus, end, accession, ref, seq, start)
  }
}

object HHBlits {

  case class HHBlitsHSP(query: HHBlitsQuery,
                        template: HHBlitsTemplate,
                        info: HHBlitsInfo,
                        agree: String,
                        description: String,
                        num: Int,
                        length: Int) extends HSP {
    def toDataTable: JsValue =
      Json.toJson(
        Map(
          "0" -> Json.toJson(Common.getCheckbox(num)),
          "1" -> Json.toJson(Common.getSingleLinkHHBlits(template.accession).toString),
          "2" -> Json.toJson(Common.addBreak(description)),
          "3" -> Json.toJson(info.probab),
          "4" -> Json.toJson(info.evalue),
          "5" -> Json.toJson(info.aligned_cols),
          "6" -> Json.toJson(template.ref)
        )
      )
  }

  case class HHBlitsInfo(aligned_cols: Int,
                         evalue: Double,
                         identities: Double,
                         probab: Double,
                         score: Double,
                         similarity: Double)

  case class HHBlitsQuery(consensus: String, end: Int, accession: String, ref: Int, seq: String, start: Int)

  case class HHBlitsTemplate(consensus: String, end: Int, accession: String, ref: Int, seq: String, start: Int)

  case class HHBlitsResult(HSPS: List[HHBlitsHSP],
                           alignment: AlignmentResult,
                           num_hits: Int,
                           query: SingleSeq,
                           db: String,
                           TMPRED: String,
                           COILPRED: String)
      extends SearchResult[HHBlitsHSP] {

    def hitsOrderBy(params: DTParam): List[HHBlitsHSP] = {
      (params.iSortCol, params.sSortDir) match {
        case (1, "asc")  => HSPS.sortBy(_.template.accession)
        case (1, "desc") => HSPS.sortWith(_.template.accession > _.template.accession)
        case (2, "asc")  => HSPS.sortBy(_.description)
        case (2, "desc") => HSPS.sortWith(_.description > _.description)
        case (3, "asc")  => HSPS.sortBy(_.info.probab)
        case (3, "desc") => HSPS.sortWith(_.info.probab > _.info.probab)
        case (4, "asc")  => HSPS.sortBy(_.info.evalue)
        case (4, "desc") => HSPS.sortWith(_.info.evalue > _.info.evalue)
        case (5, "asc")  => HSPS.sortBy(_.info.aligned_cols)
        case (5, "desc") => HSPS.sortWith(_.info.aligned_cols > _.info.aligned_cols)
        case (6, "asc")  => HSPS.sortBy(_.template.ref)
        case (6, "desc") => HSPS.sortWith(_.template.ref > _.template.ref)
        case (_, "asc")  => HSPS.sortBy(_.num)
        case (_, "desc") => HSPS.sortWith(_.num > _.num)
        case (_, _)      => HSPS.sortBy(_.num)
      }
    }

  }

}
