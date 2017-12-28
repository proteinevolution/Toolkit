package de.proteinevolution.tools.results

import javax.inject.Inject
import javax.inject.Singleton

import de.proteinevolution.tools.results.Alignment.AlignmentResult
import de.proteinevolution.tools.results.General.{ DTParam, SingleSeq }
import de.proteinevolution.tools.results.HHPred._
import play.api.libs.json._
@Singleton
class HHPred @Inject()(general: General, aln: Alignment) extends SearchTool {

  def parseResult(jsValue: JsValue): HHPredResult = {
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
      val confidence     = (x._1 \ "confidence").getOrElse(Json.toJson("")).as[String]
      HHPredHSP(queryResult, templateResult, infoResult, agree, description, num, ss_score, confidence, agree.length)
    }
    val db        = (obj \ jobID \ "db").as[String]
    val proteomes = (obj \ jobID \ "proteomes").as[String]
    val TMPRED = (obj \ jobID \ "TMPRED").asOpt[String] match {
      case Some(data) => data
      case None       => "0"
    }
    val COILPRED = (obj \ jobID \ "COILPRED").asOpt[String] match {
      case Some(data) => data
      case None       => "1"
    }

    val alignment = aln.parse((obj \ "reduced").as[JsArray])
    val query     = general.parseSingleSeq((obj \ "query").as[JsArray])
    val num_hits  = hsplist.length

    val MSA_GEN = (obj \ jobID \ "MSA_GEN").asOpt[String] match {
      case Some(data) => data
      case None       => ""
    }

    val QA3M_COUNT = (obj \ jobID \ "QA3M_COUNT").getOrElse(Json.toJson(1)).as[String].toInt

    HHPredResult(hsplist, alignment, num_hits, query, db, proteomes, TMPRED, COILPRED, MSA_GEN, QA3M_COUNT)

  }

  def parseQuery(obj: JsObject): HHPredQuery = {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end       = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = (obj \ "name").getOrElse(Json.toJson("")).as[String]
    val ref       = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq       = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val ss_dssp   = (obj \ "ss_dssp").getOrElse(Json.toJson("")).as[String]
    val ss_pred   = (obj \ "ss_pred").getOrElse(Json.toJson("")).as[String]
    val start     = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHPredQuery(consensus, end, accession, ref, seq, ss_dssp, ss_pred, start)
  }

  def parseInfo(obj: JsObject): HHPredInfo = {
    val aligned_cols = (obj \ "aligned_cols").getOrElse(Json.toJson(-1)).as[Int]
    val eval         = (obj \ "eval").getOrElse(Json.toJson(-1)).as[Double]
    val identities   = (obj \ "identities").getOrElse(Json.toJson(-1)).as[Double]
    val probab       = (obj \ "probab").getOrElse(Json.toJson(-1)).as[Double]
    val score        = (obj \ "score").getOrElse(Json.toJson(-1)).as[Double]
    val similarity   = (obj \ "similarity").getOrElse(Json.toJson(-1)).as[Double]
    HHPredInfo(aligned_cols, eval, identities, probab, score, similarity)

  }

  def parseTemplate(obj: JsObject, hits: JsObject): HHPredTemplate = {
    val consensus = (obj \ "consensus").getOrElse(Json.toJson("")).as[String]
    val end       = (obj \ "end").getOrElse(Json.toJson(-1)).as[Int]
    val accession = general.refineAccession((hits \ "struc").getOrElse(Json.toJson("")).as[String])
    val ref       = (obj \ "ref").getOrElse(Json.toJson(-1)).as[Int]
    val seq       = (obj \ "seq").getOrElse(Json.toJson("")).as[String]
    val ss_dssp   = (obj \ "ss_dssp").getOrElse(Json.toJson("")).as[String]
    val ss_pred   = (obj \ "ss_pred").getOrElse(Json.toJson("")).as[String]
    val start     = (obj \ "start").getOrElse(Json.toJson(-1)).as[Int]
    HHPredTemplate(consensus, end, accession, ref, seq, ss_dssp, ss_pred, start)
  }
}

object HHPred {
  case class HHPredHSP(query: HHPredQuery,
                       template: HHPredTemplate,
                       info: HHPredInfo,
                       agree: String,
                       description: String,
                       num: Int,
                       ss_score: Double,
                       confidence: String,
                       length: Int) {
    def toDataTable: JsValue =
      Json.toJson(
        Map(
          "0" -> Json.toJson(Common.getCheckbox(num)),
          "1" -> Json.toJson(Common.getSingleLink(template.accession).toString),
          "2" -> Json.toJson(Common.addBreakHHpred(description)),
          "3" -> Json.toJson(info.probab),
          "4" -> Json.toJson(info.evalue),
          "5" -> Json.toJson(ss_score),
          "6" -> Json.toJson(info.aligned_cols),
          "7" -> Json.toJson(template.ref)
        )
      )
  }

  case class HHPredInfo(aligned_cols: Int,
                        evalue: Double,
                        identities: Double,
                        probab: Double,
                        score: Double,
                        similarity: Double)
  case class HHPredQuery(consensus: String,
                         end: Int,
                         accession: String,
                         ref: Int,
                         seq: String,
                         ss_dssp: String,
                         ss_pred: String,
                         start: Int)
  case class HHPredTemplate(consensus: String,
                            end: Int,
                            accession: String,
                            ref: Int,
                            seq: String,
                            ss_dssp: String,
                            ss_pred: String,
                            start: Int)
  case class HHPredResult(HSPS: List[HHPredHSP],
                          alignment: AlignmentResult,
                          num_hits: Int,
                          query: SingleSeq,
                          db: String,
                          proteomes: String,
                          TMPRED: String,
                          COILPRED: String,
                          MSA_GEN: String,
                          QA3M_COUNT: Int) extends SearchResult {

    def hitsOrderBy(params: DTParam): List[HHPredHSP] = {
      (params.iSortCol, params.sSortDir) match {
        case (1, "asc")  => HSPS.sortBy(_.template.accession)
        case (1, "desc") => HSPS.sortWith(_.template.accession > _.template.accession)
        case (2, "asc")  => HSPS.sortBy(_.description)
        case (2, "desc") => HSPS.sortWith(_.description > _.description)
        case (3, "asc")  => HSPS.sortBy(_.info.probab)
        case (3, "desc") => HSPS.sortWith(_.info.probab > _.info.probab)
        case (4, "asc")  => HSPS.sortBy(_.info.evalue)
        case (4, "desc") => HSPS.sortWith(_.info.evalue > _.info.evalue)
        case (5, "asc")  => HSPS.sortBy(_.ss_score)
        case (5, "desc") => HSPS.sortWith(_.ss_score > _.ss_score)
        case (6, "asc")  => HSPS.sortBy(_.info.aligned_cols)
        case (6, "desc") => HSPS.sortWith(_.info.aligned_cols > _.info.aligned_cols)
        case (7, "asc")  => HSPS.sortBy(_.template.ref)
        case (7, "desc") => HSPS.sortWith(_.template.ref > _.template.ref)
        case (_, "asc")  => HSPS.sortBy(_.num)
        case (_, "desc") => HSPS.sortWith(_.num > _.num)
        case (_, _)      => HSPS.sortBy(_.num)
      }
    }
  }
}
