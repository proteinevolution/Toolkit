package models.database.statistics

import play.api.libs.json._
import reactivemongo.bson._

/**
  * Created by astephens on 19.02.17.
  */
case class ToolStatistic(toolID : BSONObjectID, toolName : String, current : Int, monthly : List[Int])

object ToolStatistic {
  val ID        = "toolID"
  val IDDB      = "_id"
  val TOOLNAME  = "toolName"
  val CURRENT   = "current"
  val MONTHLY   = "monthly"

  implicit object JsonReader extends Reads[ToolStatistic] {
    override def reads(json: JsValue): JsResult[ToolStatistic] = json match {
      case obj: JsObject => try {
        val toolID   = BSONObjectID.parse((obj \ ID).as[String]).getOrElse(BSONObjectID.generate())
        val toolName = (obj \ TOOLNAME).asOpt[String].getOrElse("invalid")
        val current  = (obj \ CURRENT).as[Int]
        val monthly  = (obj \ MONTHLY).as[List[Int]]
        JsSuccess(ToolStatistic(toolID, toolName, current, monthly))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JsonWriter extends Writes[ToolStatistic] {
    override def writes(toolStatistic : ToolStatistic) : JsObject = Json.obj(
      IDDB     -> toolStatistic.toolID.stringify,
      TOOLNAME -> toolStatistic.toolName,
      CURRENT  -> toolStatistic.current,
      MONTHLY  -> toolStatistic.monthly
    )
  }

  implicit object Reader extends BSONDocumentReader[ToolStatistic] {
    def read(bson : BSONDocument) : ToolStatistic = {
      ToolStatistic(
        bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        bson.getAs[String](TOOLNAME).getOrElse("invalid"),
        bson.getAs[Int](CURRENT).getOrElse(0),
        bson.getAs[List[Int]](MONTHLY).getOrElse(List.empty)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[ToolStatistic] {
    def write(toolStatistic : ToolStatistic) : BSONDocument = BSONDocument(
      IDDB     -> toolStatistic.toolID,
      TOOLNAME -> toolStatistic.toolName,
      CURRENT  -> toolStatistic.current,
      MONTHLY  -> toolStatistic.monthly
    )
  }
}