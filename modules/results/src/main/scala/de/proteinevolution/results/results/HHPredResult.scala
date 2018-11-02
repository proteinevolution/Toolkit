package de.proteinevolution.results.results

import de.proteinevolution.results.results.General.{ DTParam, SingleSeq }
import io.circe.{ Decoder, HCursor, Json }

case class HHPredResult(
    HSPS: List[HHPredHSP],
    alignment: AlignmentResult,
    num_hits: Int,
    query: SingleSeq,
    db: String,
    proteomes: String,
    TMPRED: String,
    COILPRED: String,
    MSA_GEN: String,
    QA3M_COUNT: Int
) extends SearchResult[HHPredHSP] {
  def hitsOrderBy(params: DTParam): List[HHPredHSP] = {
    (params.orderCol, params.orderDir) match {
      case (1, "asc")  => HSPS.sortBy(_.template.get.accession)
      case (1, "desc") => HSPS.sortWith(_.template.get.accession > _.template.get.accession)
      case (2, "asc")  => HSPS.sortBy(_.description)
      case (2, "desc") => HSPS.sortWith(_.description > _.description)
      case (3, "asc")  => HSPS.sortBy(_.info.get.probab)
      case (3, "desc") => HSPS.sortWith(_.info.get.probab > _.info.get.probab)
      case (4, "asc")  => HSPS.sortBy(_.info.get.eval)
      case (4, "desc") => HSPS.sortWith(_.info.get.eval > _.info.get.eval)
      case (5, "asc")  => HSPS.sortBy(_.ss_score)
      case (5, "desc") => HSPS.sortWith(_.ss_score > _.ss_score)
      case (6, "asc")  => HSPS.sortBy(_.info.get.aligned_cols)
      case (6, "desc") => HSPS.sortWith(_.info.get.aligned_cols > _.info.get.aligned_cols)
      case (7, "asc")  => HSPS.sortBy(_.template.get.ref)
      case (7, "desc") => HSPS.sortWith(_.template.get.ref > _.template.get.ref)
      case (_, "asc")  => HSPS.sortBy(_.num)
      case (_, "desc") => HSPS.sortWith(_.num > _.num)
      case (_, _)      => HSPS.sortBy(_.num)
    }
  }
}

object HHPredResult {

  implicit val hhpredResultDecoder: Decoder[HHPredResult] = (c: HCursor) =>
    for {
      jobId      <- c.downField("jobID").as[String]
      hits       <- c.downField(jobId).downField("hits").as[List[Json]]
      alignments <- c.downField(jobId).downField("alignments").as[List[Json]]
      db         <- c.downField(jobId).downField("db").as[String]
      alignment  <- c.downField("reduced").as[AlignmentResult]
      query      <- c.downField("query").as[SingleSeq]
      proteomes  <- c.downField(jobId).downField("proteomes").as[String]
      tmpred     <- c.downField(jobId).downField("TMPRED").as[Option[String]]
      coilpred   <- c.downField(jobId).downField("COILPRED").as[Option[String]]
      msa_gen    <- c.downField(jobId).downField("MSA_GEN").as[Option[String]]
      qa3m_count <- c.downField(jobId).downField("QA3M_COUNT").as[Option[Int]]
    } yield {
      val hspList = HHPredHSP.hhpredHSPListDecoder(hits, alignments)
      new HHPredResult(
        hspList,
        alignment,
        hspList.length,
        query,
        db,
        proteomes,
        tmpred.getOrElse("0"),
        coilpred.getOrElse("1"),
        msa_gen.getOrElse(""),
        qa3m_count.getOrElse(1)
      )
  }

}
