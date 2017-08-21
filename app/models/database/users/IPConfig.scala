package models.database.users

/**
  * Created by astephens on 15.08.17.
  */
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{ JsObject, Json, Writes }
import reactivemongo.bson._

case class IPConfig(id: BSONObjectID = BSONObjectID.generate(), // ID in MongoDB
                    ipHash: String,
                    openSessions: Int = 0,
                    score: Int = 0,
                    scoreMax: Int = IPConfig.scoreMaxDefault,
                    dateCreated: Option[DateTime] = Some(DateTime.now), // Creation date
                    dateUpdated: Option[DateTime] = Some(DateTime.now)) { // Last used on
  def isInLimits: Boolean = {
    score <= scoreMax &&
    openSessions <= IPConfig.sessionsMax
  }
}

object IPConfig {
  // Standard computation scoring unit
  final val sessionsMax
    : Int                           = Int.MaxValue - 1 // TODO we may need to limit the amount of sessions, but at the same time we need to reset them again - so a good limit has to be found
  final val scoreMaxDefault: Int    = 1000
  final val scoreIgnoreRequest: Int = -1

  // Constants for the JSON object identifiers // Field Description
  final val ID           = "id"           // ID in scala
  final val IDDB         = "_id"          // ID in MongoDB
  final val IPHASH       = "ipHash"       // hashed IP address
  final val OPENSESSIONS = "openSessions" // Created sessions so far
  final val SCORE        = "score"        // Score for created jobs
  final val SCOREMAX     = "scoreMax"     // Maximum score the user can have
  final val DATECREATED  = "dateCreated"  // Date the object was created
  final val DATEUPDATED  = "dateUpdated"  // Date the object was last updated

  /**
    * Define how the User object is formatted when turned into a json object
    */
  implicit object JobWrites extends Writes[IPConfig] {
    val dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")
    def writes(ipConfig: IPConfig): JsObject = Json.obj(
      ID           -> ipConfig.id.stringify,
      IPHASH       -> ipConfig.ipHash,
      OPENSESSIONS -> ipConfig.openSessions,
      SCORE        -> ipConfig.score,
      SCOREMAX     -> ipConfig.scoreMax,
      DATECREATED  -> ipConfig.dateCreated.map(dt => dtf.print(dt)),
      DATEUPDATED  -> ipConfig.dateUpdated.map(dt => dtf.print(dt))
    )
  }

  /**
    * Define how the User object is formatted in the DB
    */
  implicit object Reader extends BSONDocumentReader[IPConfig] {
    override def read(bson: BSONDocument): IPConfig =
      IPConfig(
        id = bson.getAs[BSONObjectID](IDDB).get,
        ipHash = bson.getAs[String](IPHASH).get,
        openSessions = bson.getAs[Int](OPENSESSIONS).getOrElse(0),
        score = bson.getAs[Int](SCORE).getOrElse(0),
        scoreMax = bson.getAs[Int](SCOREMAX).getOrElse(IPConfig.scoreMaxDefault),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value))
      )
  }

  implicit object Writer extends BSONDocumentWriter[IPConfig] {
    override def write(ipConfig: IPConfig): BSONDocument =
      BSONDocument(
        IDDB         -> ipConfig.id,
        IPHASH       -> ipConfig.ipHash,
        OPENSESSIONS -> ipConfig.openSessions,
        SCORE        -> ipConfig.score,
        SCOREMAX     -> ipConfig.scoreMax,
        DATECREATED  -> BSONDateTime(ipConfig.dateCreated.fold(-1L)(_.getMillis)),
        DATEUPDATED  -> BSONDateTime(ipConfig.dateUpdated.fold(-1L)(_.getMillis))
      )
  }
}
