package de.proteinevolution.user

import io.circe._
import io.circe.generic.semiauto._
import reactivemongo.bson._

case class SessionData(ip: String, userAgent: String, location: Location)

object SessionData {

  implicit val sessionDataEncoder: Encoder[SessionData] = deriveEncoder[SessionData]

  implicit val sessionDataHandler: BSONHandler[BSONDocument, SessionData] = Macros.handler[SessionData]

}
