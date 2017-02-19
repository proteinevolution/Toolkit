package models.database.statistics

import play.api.libs.json._
import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader, BSONObjectID}

/**
  * Created by astephens on 19.02.17.
  */
case class ToolCollection (toolCollectionID : BSONObjectID, toolStatistics : List[ToolStatistic])

object ToolCollection {
  val ID              = "toolCollectionID"
  val IDDB            = "_id"
  val TOOLSTATISTICS  = "toolStatistics"

  implicit object JsonReader extends Reads[ToolCollection] {
    override def reads(json: JsValue): JsResult[ToolCollection] = json match {
      case obj: JsObject => try {
        val toolID   = BSONObjectID.parse((obj \ ID).as[String]).getOrElse(BSONObjectID.generate())
        val toolStatistics = (obj \ TOOLSTATISTICS).asOpt[List[ToolStatistic]].getOrElse(List.empty)
        JsSuccess(ToolCollection(toolID, toolStatistics))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JsonWriter extends Writes[ToolCollection] {
    override def writes(toolStatistic : ToolCollection) : JsObject = Json.obj(
      IDDB           -> toolStatistic.toolCollectionID.stringify,
      TOOLSTATISTICS -> toolStatistic.toolStatistics
    )
  }

  implicit object Reader extends BSONDocumentReader[ToolCollection] {
    def read(bson : BSONDocument) : ToolCollection = {
      ToolCollection(
        toolCollectionID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        toolStatistics   = bson.getAs[List[ToolStatistic]](TOOLSTATISTICS).getOrElse(List.empty)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[ToolCollection] {
    def write(toolStatistic : ToolCollection) : BSONDocument = BSONDocument(
      IDDB           -> toolStatistic.toolCollectionID,
      TOOLSTATISTICS -> toolStatistic.toolCollectionID
    )
  }
}
