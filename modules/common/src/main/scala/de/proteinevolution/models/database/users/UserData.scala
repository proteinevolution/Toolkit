package de.proteinevolution.models.database.users

import play.api.libs.json.{ JsObject, Json, Writes }
import reactivemongo.bson.{ BSONDocument, BSONDocumentReader, BSONDocumentWriter }

case class UserData(nameLogin: String, // User Login Name
                    password: String, // Password of the User (Hashed)
                    eMail: String, // User eMail Address
                    nameFirst: Option[String] = None, // User First Name
                    nameLast: Option[String] = None, // User Last Name
                    country: Option[String] = None) // 3 Char long Optional String, Country code

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

  implicit object JobWrites extends Writes[UserData] {
    def writes(userData: UserData): JsObject = Json.obj(
      NAMELOGIN -> userData.nameLogin,
      EMAIL     -> userData.eMail,
      NAMEFIRST -> userData.nameFirst,
      NAMELAST  -> userData.nameLast,
      COUNTRY   -> userData.country
    )
  }

  /**
   * Object containing the reader for the Class
   */
  implicit object Reader extends BSONDocumentReader[UserData] {
    def read(bson: BSONDocument): UserData = UserData(
      nameLogin = bson.getAs[String](NAMELOGIN).getOrElse(""),
      password = bson.getAs[String](PASSWORD).getOrElse(""),
      eMail = bson.getAs[String](EMAIL).getOrElse(""),
      nameFirst = bson.getAs[String](NAMEFIRST),
      nameLast = bson.getAs[String](NAMELAST),
      country = bson.getAs[String](COUNTRY)
    )
  }

  /**
   * Object containing the writer for the Class
   */
  implicit object Writer extends BSONDocumentWriter[UserData] {
    def write(userData: UserData): BSONDocument = BSONDocument(
      NAMELOGIN -> userData.nameLogin,
      PASSWORD  -> userData.password,
      EMAIL     -> userData.eMail,
      NAMEFIRST -> userData.nameFirst,
      NAMELAST  -> userData.nameLast,
      COUNTRY   -> userData.country
    )
  }
}
