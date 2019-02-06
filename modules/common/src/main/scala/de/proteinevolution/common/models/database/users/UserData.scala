package de.proteinevolution.models.database.users

import io.circe.Encoder
import io.circe.generic.semiauto._
import reactivemongo.bson._

case class UserData(
    nameLogin: String,
    password: String,
    eMail: String,
    nameFirst: Option[String] = None,
    nameLast: Option[String] = None,
    country: Option[String] = None
)

object UserData {

  final val NAMELOGIN   = "nameLogin"
  final val PASSWORD    = "password"
  final val PASSWORDOLD = "passwordOld"
  final val EMAIL       = "eMail"
  final val PASSWORDNEW = "passwordNew"
  final val NAMEFIRST   = "nameFirst"
  final val NAMELAST    = "nameLast"
  final val COUNTRY     = "country"

  implicit val userDataEncoder: Encoder[UserData] = deriveEncoder[UserData]

  implicit val userDataBSONHandler: BSONHandler[BSONDocument, UserData] = Macros.handler[UserData]

}
