package de.proteinevolution.results.results

import de.proteinevolution.results.results.HHTemplate.DummyTemplate
import io.circe.Json
import io.circe.syntax._

case class PSIBlastHSP(
    eValue: Double,
    num: Int,
    bitScore: Double,
    score: Int,
    hit_start: Int,
    hit_end: Int,
    hit_seq: String,
    query_seq: String,
    query_start: Int,
    query_end: Int,
    query_id: String,
    hit_len: Int,
    gaps: Int,
    identity: Int,
    positive: Int,
    ref_len: Int,
    accession: String,
    midLine: String,
    description: String,
    info: PSIBlastInfo = PSIBlastInfo(-1, -1, -1, -1),
    template: HHTemplate = DummyTemplate()
) extends HSP {

  import SearchResultImplicits._

  def toDataTable(db: String): Json = {
    Map[String, Either[Double, String]](
      "0" -> Right(Common.getCheckbox(num)),
      "1" -> Right(Common.getSingleLinkDB(db, accession).toString),
      "2" -> Right(Common.addBreak(description.slice(0, 84))),
      "3" -> Right("%.2e".format(eValue)),
      "4" -> Left(bitScore),
      "5" -> Left(ref_len),
      "6" -> Left(hit_len)
    ).asJson
  }
}

object PSIBlastHSP {

  def parseHSP(json: Json, db: String): PSIBlastHSP = {
    val c               = json.hcursor
    val hsps            = c.downField("hsps").first
    val descriptionBase = c.downField("description").first
    for {
      eValue      <- hsps.downField("evalue")
      num         <- c.downField("num").as[Int]
      bitScore    <- hsps.downField("bit_score").as[Double]
      score       <- hsps.downField("score").as[Int]
      positive    <- hsps.downField("positive").as[Int]
      identity    <- hsps.downField("identity").as[Int]
      gaps        <- hsps.downField("gaps").as[Int]
      hit_start   <- hsps.downField("hit_from").as[Int]
      hit_end     <- hsps.downField("hit_to").as[Int]
      hit_seq     <- hsps.downField("hseq").as[String]
      query_seq   <- hsps.downField("qseq").as[String]
      query_start <- hsps.downField("query_from").as[Int]
      query_end   <- hsps.downField("query_to").as[Int]
      query_id    <- hsps.downField("query_id").as[String]
      ref_len     <- c.downField("len").as[Int]
      hit_len     <- hsps.downField("align_len").as[Int]
      midLine     <- hsps.downField("midline").as[String]
      description <- descriptionBase.downField("title").as[String]
      accession1  <- descriptionBase.downField("title").as[String]
      accession2  <- descriptionBase.downField("accession").as[String]
    } yield {
      // workaround: bug of psiblast output when searching pdb_nr
      val accession = if (db == "pdb_nr") {
        accession1.split("\\s+").head
      } else {
        General.refineAccession(accession2)
      }
      PSIBlastHSP(
        eValue,
        num,
        bitScore,
        score,
        hit_start,
        hit_end,
        hit_seq.toUpperCase,
        query_seq.toUpperCase,
        query_start,
        query_end,
        query_id,
        hit_len,
        gaps,
        identity,
        positive,
        ref_len,
        accession,
        midLine.toUpperCase,
        description
      )
    }
  }

}
