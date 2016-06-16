package models.database

import org.joda.time.DateTime

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, nonEmptyText, optional }
import play.api.data.validation.Constraints.pattern

case class MongoDBUser(id:            Option[String],
                       nameLogin:     String,
                       password:      String,
                       lastLoginDate: Option[DateTime],
                       creationDate:  Option[DateTime],
                       updateDate:    Option[DateTime])

object MongoDBUser {
  import play.api.libs.json._

  // Constants for the JSON object identifiers
  val ID            = "id"
  val IDDB          = "_id"
  val NAMELOGIN     = "nameLogin"
  val PASSWORD      = "password"
  val DATELASTLOGIN = "dateLastLogin"
  val DATECREATED   = "dateCreated"
  val DATEUPDATED   = "dateUpdated"

  implicit object UserWrites extends OWrites[MongoDBUser] {
    def writes(user: MongoDBUser): JsObject = Json.obj(
      IDDB          -> user.id,
      NAMELOGIN     -> user.nameLogin,
      PASSWORD      -> user.password,
      DATELASTLOGIN -> user.lastLoginDate.fold(-1L)(_.getMillis),
      DATECREATED   -> user.creationDate.fold(-1L)(_.getMillis),
      DATEUPDATED   -> user.updateDate.fold(-1L)(_.getMillis))
  }

  implicit object UserReads extends Reads[MongoDBUser] {
    def reads(json: JsValue): JsResult[MongoDBUser] = json match {
      case obj: JsObject => try {
        val id            = (obj \ IDDB).asOpt[String]
        val nameLogin     = (obj \ NAMELOGIN).as[String]
        val password      = (obj \ PASSWORD).as[String]
        val lastLoginDate = (obj \ DATELASTLOGIN).asOpt[Long]
        val creationDate  = (obj \ DATECREATED).asOpt[Long]
        val updateDate    = (obj \ DATEUPDATED).asOpt[Long]

        JsSuccess(
          MongoDBUser(
            id,
            nameLogin,
            password,
            lastLoginDate.map(new DateTime(_)),
            creationDate.map(new DateTime(_)),
            updateDate.map(new DateTime(_))))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }

  val form = Form(
    mapping(
      ID            -> optional(text verifying pattern("""[a-fA-F0-9]{24}""".r, error = "error.objectId")),
      NAMELOGIN     -> nonEmptyText,
      PASSWORD      -> nonEmptyText,
      DATELASTLOGIN -> optional(longNumber),
      DATECREATED   -> optional(longNumber),
      DATEUPDATED   -> optional(longNumber)) {
      (id, nameLogin, password, lastLoginDate, creationDate, updateDate) =>
        MongoDBUser(
          id,
          nameLogin,
          password,
          lastLoginDate.map(new DateTime(_)),
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_)))
    } { user =>
      Some(
        (user.id,
         user.nameLogin,
         user.password,
         user.lastLoginDate.map(_.getMillis),
         user.creationDate.map(_.getMillis),
         user.updateDate.map(_.getMillis)))
    })
}