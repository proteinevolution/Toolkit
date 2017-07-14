package models.database.statistics

import play.api.libs.json._
import reactivemongo.bson._

/**
  * Created by astephens on 19.02.17.
  */
case class ToolStatistic(statisticsID: BSONObjectID,
                         toolName: String,
                         monthly: List[Int],
                         monthlyFailed: List[Int],
                         monthlyDeleted: List[Int]) {
  def total: Long       = monthly.map(_.toLong).sum[Long]
  def totalFailed: Long = monthlyFailed.map(_.toLong).sum[Long]
}

object ToolStatistic {
  val ID             = "statisticsID"
  val IDDB           = "_id"
  val TOOLNAME       = "toolName"
  val MONTHLY        = "monthly"
  val MONTHLYFAILED  = "monthlyFailed"
  val MONTHLYDELETED = "monthlyDeleted"

  implicit object JsonWriter extends Writes[ToolStatistic] {
    override def writes(toolStatistic: ToolStatistic): JsObject = Json.obj(
      IDDB           -> toolStatistic.statisticsID.stringify,
      TOOLNAME       -> toolStatistic.toolName,
      MONTHLY        -> toolStatistic.monthly,
      MONTHLYFAILED  -> toolStatistic.monthlyFailed,
      MONTHLYDELETED -> toolStatistic.monthlyDeleted
    )
  }

  implicit object Reader extends BSONDocumentReader[ToolStatistic] {
    def read(bson: BSONDocument): ToolStatistic = {
      ToolStatistic(
        statisticsID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        toolName = bson.getAs[String](TOOLNAME).getOrElse("invalid"),
        monthly = bson.getAs[List[Int]](MONTHLY).getOrElse(List.empty),
        monthlyFailed = bson.getAs[List[Int]](MONTHLYFAILED).getOrElse(List.empty),
        monthlyDeleted = bson.getAs[List[Int]](MONTHLYDELETED).getOrElse(List.empty)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[ToolStatistic] {
    def write(toolStatistic: ToolStatistic): BSONDocument = BSONDocument(
      IDDB           -> toolStatistic.statisticsID,
      TOOLNAME       -> toolStatistic.toolName,
      MONTHLY        -> toolStatistic.monthly,
      MONTHLYFAILED  -> toolStatistic.monthlyFailed,
      MONTHLYDELETED -> toolStatistic.monthlyDeleted
    )
  }
}
