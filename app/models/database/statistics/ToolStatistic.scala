package models.database.statistics

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import reactivemongo.bson._

/**
  * Created by astephens on 19.02.17.
  */
case class ToolStatistic(toolID: BSONObjectID,
                         toolName: String,
                         current: Int,
                         currentFailed: Int,
                         monthly: List[Int],
                         monthlyFailed: List[Int],
                         datePushed: List[DateTime]) {
  def total: Long       = monthly.map(_.toLong).sum[Long]
  def totalFailed: Long = monthlyFailed.map(_.toLong).sum[Long]

  def pushMonth(): ToolStatistic = {
    this.copy(
      current = 0,
      currentFailed = 0,
      monthly = this.monthly.::(this.current),
      monthlyFailed = this.monthlyFailed.::(this.currentFailed),
      datePushed = this.datePushed.::(DateTime.now)
    )
  }
}

object ToolStatistic {
  val ID            = "toolID"
  val IDDB          = "_id"
  val TOOLNAME      = "toolName"
  val CURRENT       = "current"
  val CURRENTFAILED = "currentFailed"
  val MONTHLY       = "monthly"
  val MONTHLYFAILED = "monthlyFailed"
  val DATEPUSHED    = "datePushed"

  implicit object JsonReader extends Reads[ToolStatistic] {
    override def reads(json: JsValue): JsResult[ToolStatistic] = json match {
      case obj: JsObject =>
        try {
          val toolID        = BSONObjectID.parse((obj \ ID).as[String]).getOrElse(BSONObjectID.generate())
          val toolName      = (obj \ TOOLNAME).asOpt[String].getOrElse("invalid")
          val current       = (obj \ CURRENT).as[Int]
          val currentFailed = (obj \ CURRENT).as[Int]
          val monthly       = (obj \ MONTHLY).as[List[Int]]
          val monthlyFailed = (obj \ MONTHLYFAILED).as[List[Int]]
          val datePushed    = (obj \ DATEPUSHED).as[List[DateTime]]
          JsSuccess(ToolStatistic(toolID, toolName, current, currentFailed, monthly, monthlyFailed, datePushed))
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JsonWriter extends Writes[ToolStatistic] {
    val dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")
    override def writes(toolStatistic: ToolStatistic): JsObject = Json.obj(
      IDDB          -> toolStatistic.toolID.stringify,
      TOOLNAME      -> toolStatistic.toolName,
      CURRENT       -> toolStatistic.current,
      CURRENTFAILED -> toolStatistic.currentFailed,
      MONTHLY       -> toolStatistic.monthly,
      MONTHLYFAILED -> toolStatistic.monthlyFailed,
      DATEPUSHED -> toolStatistic.datePushed.map(dt =>
        Json.obj("string" -> dtf.print(dt), "month" -> dt.monthOfYear().getAsShortText, "year" -> dt.year().get))
    )
  }

  implicit object Reader extends BSONDocumentReader[ToolStatistic] {
    def read(bson: BSONDocument): ToolStatistic = {
      ToolStatistic(
        toolID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        toolName = bson.getAs[String](TOOLNAME).getOrElse("invalid"),
        current = bson.getAs[Int](CURRENT).getOrElse(0),
        currentFailed = bson.getAs[Int](CURRENTFAILED).getOrElse(0),
        monthly = bson.getAs[List[Int]](MONTHLY).getOrElse(List.empty),
        monthlyFailed = bson.getAs[List[Int]](MONTHLYFAILED).getOrElse(List.empty),
        datePushed = bson.getAs[List[BSONDateTime]](DATEPUSHED).getOrElse(List.empty).map(dt => new DateTime(dt.value))
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[ToolStatistic] {
    def write(toolStatistic: ToolStatistic): BSONDocument = BSONDocument(
      IDDB          -> toolStatistic.toolID,
      TOOLNAME      -> toolStatistic.toolName,
      CURRENT       -> toolStatistic.current,
      CURRENTFAILED -> toolStatistic.current,
      MONTHLY       -> toolStatistic.monthly,
      MONTHLYFAILED -> toolStatistic.monthlyFailed,
      DATEPUSHED    -> toolStatistic.datePushed.map(a => BSONDateTime(a.getMillis))
    )
  }
}
