package models.database.statistics

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import reactivemongo.bson._

/**
  * Created by astephens on 14.07.17.
  */
case class StatisticsObject(statisticsID:   BSONObjectID,
                            userStatistics: List[UserStatistic],
                            toolStatistics: List[ToolStatistic],
                            datePushed:     List[DateTime]) {

}

object StatisticsObject {
  val ID             = "statisticsID"
  val IDDB           = "_id"
  val USERSTATISTICS = "userStat"
  val TOOLSTATISTICS = "toolStat"
  val DATEPUSHED     = "datePushed"

  implicit object JsonWriter extends Writes[StatisticsObject] {
    val dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")
    override def writes(toolStatistic: StatisticsObject): JsObject = Json.obj(
      IDDB           -> toolStatistic.statisticsID.stringify,
      USERSTATISTICS -> toolStatistic.toolStatistics,
      TOOLSTATISTICS -> toolStatistic.userStatistics,
      DATEPUSHED -> toolStatistic.datePushed.map(
        dt => Json.obj("string" -> dtf.print(dt), "month" -> dt.monthOfYear().getAsShortText, "year" -> dt.year().get)
      )
    )
  }

  implicit object Reader extends BSONDocumentReader[StatisticsObject] {
    def read(bson: BSONDocument): StatisticsObject = {
      StatisticsObject(
        statisticsID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        userStatistics = bson.getAs[List[UserStatistic]](USERSTATISTICS).getOrElse(List.empty),
        toolStatistics = bson.getAs[List[ToolStatistic]](TOOLSTATISTICS).getOrElse(List.empty),
        datePushed = bson.getAs[List[BSONDateTime]](DATEPUSHED).getOrElse(List.empty).map(dt => new DateTime(dt.value))
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[StatisticsObject] {
    def write(toolStatistic: StatisticsObject): BSONDocument = BSONDocument(
      IDDB           -> toolStatistic.statisticsID,
      USERSTATISTICS -> toolStatistic.userStatistics,
      TOOLSTATISTICS -> toolStatistic.toolStatistics,
      DATEPUSHED     -> toolStatistic.datePushed.map(a => BSONDateTime(a.getMillis))
    )
  }
}

