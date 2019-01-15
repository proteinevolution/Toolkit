package de.proteinevolution.results.results

import de.proteinevolution.results.results.General.{DTParam, SingleSeq}
import io.circe.{Decoder, HCursor, Json}

case class HHompResult(
    HSPS: List[HHompHSP],
    num_hits: Int,
    query: SingleSeq,
    db: String,
    overall_prob: Double,
    alignment: AlignmentResult = AlignmentResult(Nil)
) extends SearchResult[HHompHSP] {
  def hitsOrderBy(params: DTParam): List[HHompHSP] = {
    (params.orderCol, params.orderDir) match {
      case (1, "asc")  => HSPS.sortBy(_.template.accession)
      case (1, "desc") => HSPS.sortWith(_.template.accession > _.template.accession)
      case (2, "asc")  => HSPS.sortBy(_.description)
      case (2, "desc") => HSPS.sortWith(_.description > _.description)
      case (3, "asc")  => HSPS.sortBy(_.info.probab_hit)
      case (3, "desc") => HSPS.sortWith(_.info.probab_hit > _.info.probab_hit)
      case (4, "asc")  => HSPS.sortBy(_.info.probab_OMP)
      case (4, "desc") => HSPS.sortWith(_.info.probab_OMP > _.info.probab_OMP)
      case (5, "asc")  => HSPS.sortBy(_.info.eval)
      case (5, "desc") => HSPS.sortWith(_.info.eval > _.info.eval)
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

object HHompResult {

  implicit val hhompResultDecoder: Decoder[HHompResult] = (c: HCursor) =>
    for {
      jobId       <- c.downField("jobID").as[String]
      hits        <- c.downField(jobId).downField("hits").as[List[Json]]
      alignments  <- c.downField(jobId).downField("alignments").as[List[Json]]
      db          <- c.downField(jobId).downField("db").as[String]
      query       <- c.downField("query").as[SingleSeq]
      overallProb <- c.downField(jobId).downField("overallprob").as[Double]
    } yield {
      val hspList = HHompHSP.hhompHSPListDecoder(hits, alignments)
      new HHompResult(hspList, hspList.length, query, db, overallProb)
  }

}
