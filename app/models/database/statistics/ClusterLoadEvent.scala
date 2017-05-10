package models.database.statistics

import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json, Writes}
import reactivemongo.bson._

/**
  * Created by astephens on 27.03.17.
  */
case class ClusterLoadEvent(id: BSONObjectID,
                            listLoad: List[Double],
                            averageLoad: Double,
                            timestamp: Option[DateTime] = Some(DateTime.now()))

object ClusterLoadEvent {
  val IDDB      = "_id"
  val LISTLOAD  = "listLoad"
  val AVERAGE   = "averageLoad"
  val TIMESTAMP = "timestamp"

  implicit object JsonWriter extends Writes[ClusterLoadEvent] {
    override def writes(clusterLoadEvent: ClusterLoadEvent): JsObject = Json.obj(
      IDDB      -> clusterLoadEvent.id.stringify,
      LISTLOAD  -> clusterLoadEvent.listLoad,
      AVERAGE   -> clusterLoadEvent.averageLoad,
      TIMESTAMP -> clusterLoadEvent.timestamp
    )
  }

  implicit object Reader extends BSONDocumentReader[ClusterLoadEvent] {
    def read(bson: BSONDocument): ClusterLoadEvent = {
      ClusterLoadEvent(
        bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        bson.getAs[List[Double]](LISTLOAD).getOrElse(List.empty[Double]),
        bson.getAs[Double](AVERAGE).getOrElse(0.0),
        bson.getAs[BSONDateTime](TIMESTAMP).map(dt => new DateTime(dt.value))
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[ClusterLoadEvent] {
    def write(clusterLoadEvent: ClusterLoadEvent): BSONDocument = BSONDocument(
      IDDB      -> clusterLoadEvent.id,
      LISTLOAD  -> clusterLoadEvent.listLoad,
      AVERAGE   -> clusterLoadEvent.averageLoad,
      TIMESTAMP -> BSONDateTime(clusterLoadEvent.timestamp.fold(-1L)(_.getMillis))
    )
  }
}
