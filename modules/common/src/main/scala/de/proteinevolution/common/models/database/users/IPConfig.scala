package de.proteinevolution.models.database.users

import java.time.ZonedDateTime

import de.proteinevolution.models.util.{ ZonedDateTimeHelper => h }
import io.circe.{ Encoder, Json }
import reactivemongo.bson._

case class IPConfig(
    id: BSONObjectID = BSONObjectID.generate(),
    ipHash: String,
    openSessions: Int = 0,
    score: Int = 0,
    scoreMax: Int = IPConfig.scoreMaxDefault,
    dateCreated: Option[ZonedDateTime] = Some(ZonedDateTime.now),
    dateUpdated: Option[ZonedDateTime] = Some(ZonedDateTime.now)
) { // Last used on
  def isInLimits: Boolean = {
    score <= scoreMax &&
    openSessions <= IPConfig.sessionsMax
  }
}

object IPConfig {

  final val sessionsMax: Int        = Int.MaxValue - 1
  final val scoreMaxDefault: Int    = 1000
  final val scoreIgnoreRequest: Int = -1

  final val ID           = "id"
  final val IDDB         = "_id"
  final val IPHASH       = "ipHash"
  final val OPENSESSIONS = "openSessions"
  final val SCORE        = "score"
  final val SCOREMAX     = "scoreMax"
  final val DATECREATED  = "dateCreated"
  final val DATEUPDATED  = "dateUpdated"

  implicit val ipConfigEncoder: Encoder[IPConfig] = (conf: IPConfig) =>
    Json.obj(
      (ID, Json.fromString(conf.id.stringify)),
      (IPHASH, Json.fromString(conf.ipHash)),
      (OPENSESSIONS, Json.fromInt(conf.openSessions)),
      (SCORE, Json.fromInt(conf.score)),
      (SCOREMAX, Json.fromInt(conf.scoreMax)),
      (DATECREATED, conf.dateCreated.map(zdt => Json.fromString(zdt.format(h.dateTimeFormatter))).getOrElse(Json.Null)),
      (DATEUPDATED, conf.dateUpdated.map(zdt => Json.fromString(zdt.format(h.dateTimeFormatter))).getOrElse(Json.Null))
  )

  implicit object Reader extends BSONDocumentReader[IPConfig] {
    override def read(bson: BSONDocument): IPConfig =
      IPConfig(
        id = bson.getAs[BSONObjectID](IDDB).get,
        ipHash = bson.getAs[String](IPHASH).get,
        openSessions = bson.getAs[Int](OPENSESSIONS).getOrElse(0),
        score = bson.getAs[Int](SCORE).getOrElse(0),
        scoreMax = bson.getAs[Int](SCOREMAX).getOrElse(IPConfig.scoreMaxDefault),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => h.getZDT(dt)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => h.getZDT(dt))
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
        DATECREATED  -> BSONDateTime(ipConfig.dateCreated.fold(-1L)(_.toInstant.toEpochMilli)),
        DATEUPDATED  -> BSONDateTime(ipConfig.dateUpdated.fold(-1L)(_.toInstant.toEpochMilli))
      )
  }

}
