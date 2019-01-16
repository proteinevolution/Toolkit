package de.proteinevolution.results.results

import io.circe.syntax._
import io.circe.{ Decoder, HCursor, Json }

case class HmmerHSP(
    eValue: Double,
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
    domain_obs_num: Int
) extends HSP {
  def toDataTable(db: String): Json = {
    import SearchResultImplicits._
    Map[String, Either[Either[Double, Int], String]](
      "0" -> Right(Common.getCheckbox(num)),
      "1" -> Right(Common.getSingleLinkDB(db, accession).toString),
      "2" -> Right(Common.addBreak(description.slice(0, 84))),
      "3" -> Left(Left(full_evalue)),
      "4" -> Left(Left(eValue)),
      "5" -> Left(Left(bitscore)),
      "6" -> Left(Right(hit_len))
    ).asJson
  }
}

object HmmerHSP {

  implicit val hmmerHSPDecoder: Decoder[HmmerHSP] =
    (c: HCursor) =>
      for {
        evalue         <- c.downField("evalue").as[Double]
        full_evalue    <- c.downField("full_evalue").as[Double]
        num            <- c.downField("num").as[Int]
        bitscore       <- c.downField("bitscore").as[Double]
        hit_start      <- c.downField("hit_start").as[Int]
        hit_end        <- c.downField("hit_end").as[Int]
        hit_seq        <- c.downField("hit_seq").as[String]
        query_seq      <- c.downField("query_seq").as[String]
        query_start    <- c.downField("query_start").as[Int]
        query_end      <- c.downField("query_end").as[Int]
        query_id       <- c.downField("query_id").as[String]
        hit_len        <- c.downField("hit_len").as[Int]
        hit_id         <- c.downField("hit_id").as[String]
        midline        <- c.downField("aln_ann").downField("PP").as[String]
        description    <- c.downField("hit_description").as[String]
        domain_obs_num <- c.downField("domain_obs_num").as[Int]
      } yield {
        val accession = General.refineAccession(hit_id)
        new HmmerHSP(
          evalue,
          full_evalue,
          num,
          bitscore,
          hit_start,
          hit_end,
          hit_seq.toUpperCase,
          query_seq.toUpperCase,
          query_start,
          query_end,
          query_id,
          hit_len,
          accession,
          midline.toUpperCase,
          description,
          domain_obs_num
        )
    }

}
