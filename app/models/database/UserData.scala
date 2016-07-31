package models.database

import play.api.data._
import play.api.data.Forms.{ text, nonEmptyText, mapping, optional }
import play.api.data.validation.Constraints.pattern
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, BSONDocument}

case class UserData(nameFirst:     Option[String],    // User First Name
                    nameLast:      Option[String],    // User Last Name
                    eMail:         String,            // User eMail Address
                    institute:     Option[String],    // User Workplace
                    street:        Option[String],    // User Location
                    city:          Option[String],
                    country:       Option[String],
                    groups:        Option[String],    // Group the User is in
                    roles:         Option[String])    // Position the User is in

object UserData {
  // Constants for the BSON object identifiers
  val NAMEFIRST  = "nameFirst"
  val NAMELAST   = "nameLast"
  val EMAIL      = "eMail"
  val INSTITUTE  = "institute"
  val STREET     = "street"
  val CITY       = "city"
  val COUNTRY    = "country"
  val GROUPS     = "groups"
  val ROLES      = "roles"

  /**
    * Object containing the reader for the Class
    */
  implicit object Reader extends BSONDocumentReader[UserData] {
    def read(bson: BSONDocument): UserData =
      UserData(
        nameFirst  = bson.getAs[String](NAMEFIRST),
        nameLast   = bson.getAs[String](NAMELAST),
        eMail      = bson.getAs[String](EMAIL).get,
        institute  = bson.getAs[String](INSTITUTE),
        street     = bson.getAs[String](STREET),
        city       = bson.getAs[String](CITY),
        country    = bson.getAs[String](COUNTRY),
        groups     = bson.getAs[String](GROUPS),
        roles      = bson.getAs[String](ROLES))
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[UserData] {
    def write(userData: UserData): BSONDocument = BSONDocument(
      NAMEFIRST  -> userData.nameFirst,
      NAMELAST   -> userData.nameFirst,
      EMAIL      -> userData.eMail,
      INSTITUTE  -> userData.institute,
      STREET     -> userData.street,
      CITY       -> userData.city,
      COUNTRY    -> userData.country,
      GROUPS     -> userData.groups,
      ROLES      -> userData.roles)
  }

  /**
    * Edit form for the profile
    */
  val formProfileEdit = Form(
    mapping(
      NAMEFIRST -> optional(text),
      NAMELAST  -> optional(text),
      EMAIL     -> nonEmptyText,
      INSTITUTE -> optional(text),
      STREET    -> optional(text),
      CITY      -> optional(text),
      COUNTRY   -> optional(text),
      GROUPS    -> optional(text),
      ROLES     -> optional(text)) {
      (nameFirst, nameLast, eMail, institute, street, city, country, groups, roles) =>
        UserData(
          nameFirst,
          nameLast,
          eMail,
          institute,
          street,
          city,
          country,
          groups,
          roles)
    } { userData =>
      Some((
        userData.nameFirst,
        userData.nameLast,
        userData.eMail,
        userData.institute,
        userData.street,
        userData.city,
        userData.country,
        userData.groups,
        userData.roles
        ))
    })
}