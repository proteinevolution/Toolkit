package de.proteinevolution.models.database.users

import play.api.libs.json._
import reactivemongo.bson._

case class UserData(
    nameLogin: String, // User Login Name
    password: String, // Password of the User (Hashed)
    eMail: String, // User eMail Address
    nameFirst: Option[String] = None, // User First Name
    nameLast: Option[String] = None, // User Last Name
    country: Option[String] = None
) // 3 Char long Optional String, Country code

object UserData {
  // Constants for the BSON object identifiers
  final val NAMELOGIN   = "nameLogin"
  final val PASSWORD    = "password"
  final val PASSWORDOLD = "passwordOld"
  final val EMAIL       = "eMail"
  final val PASSWORDNEW = "passwordNew"
  final val NAMEFIRST   = "nameFirst"
  final val NAMELAST    = "nameLast"
  final val COUNTRY     = "country"

  implicit val userDataWrites: OWrites[UserData] = Json.writes[UserData]

  implicit val userDataBSONHandler: BSONHandler[BSONDocument, UserData] = Macros.handler[UserData]

}
