package models.database.users

import play.api.libs.json.{Json, JsObject, Writes}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter}

case class UserData(nameLogin : String,                // User Login Name
                    password  : String,                // Password of the User (Hashed)
                    eMail     : String,          // User eMail Address
                    nameFirst : Option[String] = None, // User First Name
                    nameLast  : Option[String] = None, // User Last Name
                    institute : Option[String] = None, // User Workplace
                    street    : Option[String] = None, // User Location
                    city      : Option[String] = None, // User / Institute city
                    country   : Option[String] = None, // 3 Char long Optional String, Country code
                    groups    : Option[String] = None, // Group the User is in
                    roles     : Option[String] = None) // Position the User is in

object UserData {
  // Constants for the BSON object identifiers
  final val NAMELOGIN   = "nameLogin"
  final val PASSWORD    = "password"
  final val PASSWORDOLD = "passwordOld"
  final val EMAIL       = "eMail"
  final val PASSWORDNEW = "passwordNew"
  final val NAMEFIRST   = "nameFirst"
  final val NAMELAST    = "nameLast"
  final val INSTITUTE   = "institute"
  final val STREET      = "street"
  final val CITY        = "city"
  final val COUNTRY     = "country"
  final val GROUPS      = "groups"
  final val ROLES       = "roles"

  implicit object JobWrites extends Writes[UserData] {
    def writes (userData : UserData) : JsObject = Json.obj(
      NAMELOGIN  -> userData.nameLogin,
      EMAIL      -> userData.eMail,
      NAMEFIRST  -> userData.nameFirst,
      NAMELAST   -> userData.nameLast,
      INSTITUTE  -> userData.institute,
      STREET     -> userData.street,
      CITY       -> userData.city,
      COUNTRY    -> userData.country,
      GROUPS     -> userData.groups,
      ROLES      -> userData.roles
    )
  }

  /**
    * Object containing the reader for the Class
    */
  implicit object Reader extends BSONDocumentReader[UserData] {
    def read(bson: BSONDocument): UserData = UserData(
        nameLogin  = bson.getAs[String](NAMELOGIN).getOrElse(""),
        password   = bson.getAs[String](PASSWORD).getOrElse(""),
        eMail      = bson.getAs[String](EMAIL).getOrElse(""),
        nameFirst  = bson.getAs[String](NAMEFIRST),
        nameLast   = bson.getAs[String](NAMELAST),
        institute  = bson.getAs[String](INSTITUTE),
        street     = bson.getAs[String](STREET),
        city       = bson.getAs[String](CITY),
        country    = bson.getAs[String](COUNTRY),
        groups     = bson.getAs[String](GROUPS),
        roles      = bson.getAs[String](ROLES)
    )
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[UserData] {
    def write(userData: UserData): BSONDocument = BSONDocument(
      NAMELOGIN  -> userData.nameLogin,
      PASSWORD   -> userData.password,
      EMAIL      -> userData.eMail,
      NAMEFIRST  -> userData.nameFirst,
      NAMELAST   -> userData.nameLast,
      INSTITUTE  -> userData.institute,
      STREET     -> userData.street,
      CITY       -> userData.city,
      COUNTRY    -> userData.country,
      GROUPS     -> userData.groups,
      ROLES      -> userData.roles
    )
  }
}