package de.proteinevolution.results.results

import de.proteinevolution.results.results.General.{ DTParam, SingleSeq }
import io.circe._
import io.circe.syntax._

case class PSIBlastResult(
    HSPS: List[PSIBlastHSP],
    num_hits: Int,
    iter_num: Int,
    db: String,
    eValue: Double,
    query: SingleSeq,
    belowEvalThreshold: Int,
    TMPRED: String,
    COILPRED: String,
    alignment: AlignmentResult = AlignmentResult(Nil)
) extends SearchResult[PSIBlastHSP] {
  override def hitsOrderBy(params: DTParam): List[PSIBlastHSP] = {
    (params.orderCol, params.orderDir) match {
      case (1, "asc")  => HSPS.sortBy(_.accession)
      case (1, "desc") => HSPS.sortWith(_.accession > _.accession)
      case (2, "asc")  => HSPS.sortBy(_.description)
      case (2, "desc") => HSPS.sortWith(_.description > _.description)
      case (3, "asc")  => HSPS.sortBy(_.eValue)
      case (3, "desc") => HSPS.sortWith(_.eValue > _.eValue)
      case (4, "asc")  => HSPS.sortBy(_.bitScore)
      case (4, "desc") => HSPS.sortWith(_.bitScore > _.bitScore)
      case (5, "asc")  => HSPS.sortBy(_.ref_len)
      case (5, "desc") => HSPS.sortWith(_.ref_len > _.ref_len)
      case (6, "asc")  => HSPS.sortBy(_.hit_len)
      case (6, "desc") => HSPS.sortWith(_.hit_len > _.hit_len)
      case (_, "asc")  => HSPS.sortBy(_.num)
      case (_, "desc") => HSPS.sortWith(_.num > _.num)
      case (_, _)      => HSPS.sortBy(_.num)
    }
  }
}

object PSIBlastResult {

  implicit val decodePsiBlastResult: Decoder[PSIBlastResult] = (c: HCursor) =>
    for {
      query <- c.downField("query").as[SingleSeq]
      iter_num <- c
        .downField("output_psiblastp")
        .downField("BlastOutput2")
        .first
        .downField("report")
        .downField("results")
        .downField("iterations")
        .as[List[JsonObject]]
      db     <- c.downField("output_psiblastp").downField("db").as[String]
      eValue <- c.downField("output_psiblastp").downField("evalue").as[String]
      hits <- c
        .downField("output_psiblastp")
        .downField("BlastOutput2")
        .first
        .downField("report")
        .downField("iterations")
        .rightN(iter_num.size - 1)
        .downField("search")
        .downField("hits")
        .as[List[JsonObject]]
      num_hits = hits.length
      hspList <- PSIBlastHSP.parseHSP(hit.asJson, db) // TODO !!! 
      tmpred   <- c.downField("output_psiblastp").downField("TMPRED").as[Option[String]]
      coilpred <- c.downField("output_psiblastp").downField("COILPRED").as[Option[String]]
      upperBound = calculateUpperBound(hits, eValue).getOrElse(hspList.length + 1)
    } yield {
      new PSIBlastResult(
        hspList,
        num_hits,
        iter_num.size - 1,
        db,
        eValue.toDouble,
        query,
        upperBound,
        tmpred.getOrElse("0"),
        coilpred.getOrElse("1")
      )
  }

  private def calculateUpperBound(hits: List[JsonObject], eValue: String): Option[Int] = {
    // take the smallest value above the threshold
    (for {
      hit <- hits
      cursor = hit.asJson.hcursor
      eval <- cursor.downField("hsps").first.downField("evalue").as[Double].toOption
      num  <- cursor.downField("num").as[Int].toOption
      if eval >= eValue.toDouble
    } yield {
      (eval, num)
    }).sortWith(_._1 < _._1).toMap.headOption.map(_._2)
  }

}
