package models.database.users

/**
  * Created by astephens on 15.08.17.
  */

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{ JsObject, Json, Writes }
import reactivemongo.bson._

case class IPConfig(id          : BSONObjectID = BSONObjectID.generate(), // ID in MongoDB
                    hashIP      : String,
                    score       : Int = 0,
                    scoreMax    : Int = IPConfig.scoreMaxDef,
                    dateCreated : Option[DateTime] = Some(DateTime.now), // Creation date
                    dateUpdated : Option[DateTime] = Some(DateTime.now)) { // Last used on

}

object IPConfig {
  // Standard computation scoring unit
  final val scoreMaxDef        : Int = 1000
  final val scoreIgnoreRequest : Int = -1

  // Constants for the JSON object identifiers
  final val ID            = "id" // name for the ID in scala
  final val IDDB          = "_id" //              ID in MongoDB
  final val HASHIP        = "hashIP"
  final val SCORE         = "score"
  final val SCOREMAX      = "scoreMax"
  final val DATECREATED   = "dateCreated"
  final val DATEUPDATED   = "dateUpdated"

  /**
    * Define how the User object is formatted when turned into a json object
    */
  implicit object JobWrites extends Writes[IPConfig] {
    val dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")
    def writes(user: IPConfig): JsObject = Json.obj(
      ID            -> user.id.stringify,
      HASHIP        -> user.hashIP,
      SCORE         -> user.score,
      SCOREMAX      -> user.scoreMax,
      DATECREATED   -> user.dateCreated.map(dt => dtf.print(dt)),
      DATEUPDATED   -> user.dateUpdated.map(dt => dtf.print(dt))
    )
  }

  /**
    * Define how the User object is formatted in the DB
    */
  implicit object Reader extends BSONDocumentReader[IPConfig] {
    override def read(bson: BSONDocument): IPConfig =
      IPConfig(
        id          = bson.getAs[BSONObjectID](IDDB).get,
        hashIP      = bson.getAs[String](HASHIP).get,
        score       = bson.getAs[Int](SCORE).get,
        scoreMax    = bson.getAs[Int](SCOREMAX).get,
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value))
      )
  }

  implicit object Writer extends BSONDocumentWriter[IPConfig] {
    override def write(user: IPConfig): BSONDocument =
      BSONDocument(
        IDDB          -> user.id,
        HASHIP        -> user.hashIP,
        SCORE         -> user.score,
        SCOREMAX      -> user.scoreMax,
        DATECREATED   -> BSONDateTime(user.dateCreated.fold(-1L)(_.getMillis)),
        DATEUPDATED   -> BSONDateTime(user.dateUpdated.fold(-1L)(_.getMillis))
      )
  }
}
