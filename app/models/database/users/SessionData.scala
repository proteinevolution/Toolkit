package models.database.users

import reactivemongo.bson._

/**
  * Created by astephens on 01.03.16.
  * Session object used for a simple creation of a session cookie with the sessionID
  */

case class SessionData(ip        : String,
                       userAgent : String,
                       location  : Location)

object SessionData {
  final val IP        = "ip"
  final val USERAGENT = "userAgent"
  final val LOCATION  = "location"


  implicit object Reader extends BSONDocumentReader[SessionData] {
    override def read(bson: BSONDocument): SessionData = SessionData(
      ip        = bson.getAs[String](IP).getOrElse("none"),
      userAgent = bson.getAs[String](USERAGENT).getOrElse("none"),
      location  = bson.getAs[Location](LOCATION).getOrElse(Location("none",None,None,None))
    )
  }

  implicit object Writer extends BSONDocumentWriter [SessionData] {
    override def write(sessionData : SessionData) : BSONDocument = BSONDocument(
      IP        -> sessionData.ip,
      USERAGENT -> sessionData.userAgent,
      LOCATION  -> sessionData.location
    )
  }
}