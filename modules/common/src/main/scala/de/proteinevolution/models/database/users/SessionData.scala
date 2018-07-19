package de.proteinevolution.models.database.users

import play.api.libs.json.{ JsObject, Json, Writes }
import reactivemongo.bson._

/**
 * Session object used for a simple creation of a session cookie with the sessionID
 */
case class SessionData(ip: String, userAgent: String, location: Location)

object SessionData {

  implicit object JobWrites extends Writes[SessionData] {
    def writes(sessionData: SessionData): JsObject = Json.obj(
      "ip"        -> sessionData.ip,
      "userAgent" -> sessionData.userAgent,
      "location"  -> s"${sessionData.location.country} - ${sessionData.location.city.getOrElse("/")}"
    )
  }

  implicit val sessionDataHandler: BSONHandler[BSONDocument, SessionData] =
    Macros.handler[SessionData]

}
