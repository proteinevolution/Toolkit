package de.proteinevolution.results.results

import de.proteinevolution.results.results.General.{ DTParam, SingleSeq }
import io.circe.{ Decoder, HCursor, Json }

case class HHBlitsResult(
    HSPS: List[HHBlitsHSP],
    alignment: AlignmentResult,
    num_hits: Int,
    query: SingleSeq,
    db: String,
    TMPRED: String,
    COILPRED: String
) extends SearchResult[HHBlitsHSP] {

  def hitsOrderBy(params: DTParam): List[HHBlitsHSP] = {
    (params.orderCol, params.orderDir) match {
      case (1, "asc")  => HSPS.sortBy(_.template.get.accession)
      case (1, "desc") => HSPS.sortWith(_.template.get.accession > _.template.get.accession)
      case (2, "asc")  => HSPS.sortBy(_.description)
      case (2, "desc") => HSPS.sortWith(_.description > _.description)
      case (3, "asc")  => HSPS.sortBy(_.info.get.probab)
      case (3, "desc") => HSPS.sortWith(_.info.get.probab > _.info.get.probab)
      case (4, "asc")  => HSPS.sortBy(_.info.get.eval)
      case (4, "desc") => HSPS.sortWith(_.info.get.eval > _.info.get.eval)
      case (5, "asc")  => HSPS.sortBy(_.info.get.aligned_cols)
      case (5, "desc") => HSPS.sortWith(_.info.get.aligned_cols > _.info.get.aligned_cols)
      case (6, "asc")  => HSPS.sortBy(_.template.get.ref)
      case (6, "desc") => HSPS.sortWith(_.template.get.ref > _.template.get.ref)
      case (_, "asc")  => HSPS.sortBy(_.num)
      case (_, "desc") => HSPS.sortWith(_.num > _.num)
      case (_, _)      => HSPS.sortBy(_.num)
    }
  }

}

object HHBlitsResult {

  implicit val hhblitsResultDecoder: Decoder[HHBlitsResult] = (c: HCursor) =>
    for {
      jobId      <- c.downField("jobID").as[String]
      hits       <- c.downField(jobId).downField("hits").as[List[Json]]
      alignments <- c.downField(jobId).downField("alignments").as[List[Json]]
      db         <- c.downField(jobId).downField("db").as[String]
      alignment  <- c.downField("reduced").as[AlignmentResult]
      query      <- c.downField("query").as[SingleSeq]
      tmpred     <- c.downField(jobId).downField("TMPRED").as[Option[String]]
      coilpred   <- c.downField(jobId).downField("COILPRED").as[Option[String]]
    } yield {
      val hspList = HHBlitsHSP.hhblitsHSPListDecoder(hits, alignments)
      new HHBlitsResult(hspList, alignment, hspList.length, query, db, tmpred.getOrElse("0"), coilpred.getOrElse("1"))
  }

}
