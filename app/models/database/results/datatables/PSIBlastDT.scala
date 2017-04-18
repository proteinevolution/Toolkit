package models.database.results.datatables

import play.api.libs.json.{JsObject, Json, Writes}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader}



case class PSIBlastDT(num: Int, accession: String, title: String, evalue: Double, score: Int, bitscore: Double, identity : Int, length: Int)


object PSIBlastDT {

  val num = "num"
  val accession = "description.0.accession"
  val title = "description.0.title"
  val evalue = "hsps.0.evalue"
  val score = "hsps.0.score"
  val bitscore = "hsps.0.bit_score"
  val identity = "hsps.0.identity"
  val len = "len"


  implicit object Reader extends BSONDocumentReader[PSIBlastDT] {
    override def read(bson: BSONDocument): PSIBlastDT = PSIBlastDT(
      bson.getAs[Int](num).getOrElse(-1),
      bson.getAs[String](accession).getOrElse(""),
      bson.getAs[String](title).getOrElse(""),
      bson.getAs[Double](evalue).getOrElse(-1),
      bson.getAs[Int](score).getOrElse(-1),
      bson.getAs[Double](bitscore).getOrElse(-1),
      bson.getAs[Int](identity).getOrElse(-1),
      bson.getAs[Int](len).getOrElse(-1)
    )
  }

  implicit object DataTableWrites extends Writes[PSIBlastDT] {
    def writes (datatable : PSIBlastDT) : JsObject = Json.obj(
      num       -> datatable.num,
      accession -> datatable.accession,
      title     -> datatable.title,
      evalue    -> datatable.evalue,
      score     -> datatable.score,
      bitscore  -> datatable.bitscore,
      identity  -> datatable.identity,
      len       -> datatable.length
    )
  }

}