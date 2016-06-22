package models.database

import org.joda.time.DateTime

import play.api.data._
import play.api.data.Forms.{ text, nonEmptyText, longNumber, mapping, optional }
import play.api.data.validation.Constraints.pattern

case class MongoDBUserData(id:            Option[String],
                           nameFirst:     String,
                           nameLast:      String,
                           eMail:         String,
                           institute:     String,
                           street:        String,
                           city:          String,
                           country:       String,
                           groups:        String,
                           roles:         String)

object MongoDBUserData {
  import play.api.libs.json._

  // Constants for the JSON object identifiers
  val ID         = "id"
  val IDDB       = "_id"
  val NAMEFIRST  = "nameFirst"
  val NAMELAST   = "nameLast"
  val EMAIL      = "eMail"
  val INSTITUTE  = "institute"
  val STREET     = "street"
  val CITY       = "city"
  val COUNTRY    = "country"
  val GROUPS     = "groups"
  val ROLES      = "roles"

  implicit object UserDataWrites extends OWrites[MongoDBUserData] {
    def writes(userData: MongoDBUserData): JsObject = Json.obj(
      IDDB       -> userData.id,
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

  implicit object UserDataReads extends Reads[MongoDBUserData] {
    def reads(json: JsValue): JsResult[MongoDBUserData] = json match {
      case obj: JsObject => try {
        val id         = (obj \ IDDB).asOpt[String]
        val nameFirst  = (obj \ NAMEFIRST).as[String]
        val nameLast   = (obj \ NAMELAST).as[String]
        val eMail      = (obj \ EMAIL).as[String]
        val institute  = (obj \ INSTITUTE).as[String]
        val street     = (obj \ STREET).as[String]
        val city       = (obj \ CITY).as[String]
        val country    = (obj \ COUNTRY).as[String]
        val groups     = (obj \ GROUPS).as[String]
        val roles      = (obj \ ROLES).as[String]

        JsSuccess(
          MongoDBUserData(
            id,
            nameFirst,
            nameLast,
            eMail,
            institute,
            street,
            city,
            country,
            groups,
            roles))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

  val form = Form(
    mapping(
      ID        -> optional(text verifying pattern("""[a-fA-F0-9]{24}""".r, error = "error.objectId")),
      NAMEFIRST -> text,
      NAMELAST  -> text,
      EMAIL     -> nonEmptyText,
      INSTITUTE -> text,
      STREET    -> text,
      CITY      -> text,
      COUNTRY   -> text,
      GROUPS    -> text,
      ROLES     -> text) {
      (id, nameFirst, nameLast, eMail, institute, street, city, country, groups, roles) =>
        MongoDBUserData(
          id,
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
        userData.id,
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