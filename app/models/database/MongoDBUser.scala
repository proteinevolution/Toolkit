package models.database

import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, boolean, optional }
import play.api.data.validation.Constraints.pattern

case class MongoDBUser(id            : Option[String],
                       nameLogin     : String,
                       password      : String,
                       accountType   : Short,
                       lastLoginDate : Option[DateTime],
                       creationDate  : Option[DateTime],
                       updateDate    : Option[DateTime]) {

  def checkPassword(plainPassword: String) : Boolean = {
    BCrypt.checkpw(plainPassword, password)
  }
}

object MongoDBUser {
  import play.api.libs.json._

  // Number of rounds for BCrypt to hash the Password (2^x) TODO Move to the config?
  val LOG_ROUNDS : Int = 10

  // Constants for the JSON object identifiers
  val ID            = "id"            // name for the ID in scala
  val IDDB          = "_id"           //              ID in MongoDB
  val NAMELOGIN     = "nameLogin"     //              login name field
  val PASSWORD      = "password"      //              password field
  val ACCOUNTTYPE   = "accountType"   //              account type field
  val ACCEPTEDTOS   = "acceptToS"     // needed for checking if the TOS was accepted
  val DATELASTLOGIN = "dateLastLogin" // name for the last login field
  val DATECREATED   = "dateCreated"   //              account created on field
  val DATEUPDATED   = "dateUpdated"   //              account data changed on field


  /**
    * Define how the User object is formatted
    */
  implicit object UserFormat extends OFormat[MongoDBUser] {
    override def reads(json: JsValue): JsResult[MongoDBUser] = json match {
      case obj: JsObject => try {
        val id            = (obj \ IDDB).asOpt[String]
        val nameLogin     = (obj \ NAMELOGIN).as[String]
        val password      = (obj \ PASSWORD).as[String]
        val accountType   = (obj \ ACCOUNTTYPE).as[Short]
        val lastLoginDate = (obj \ DATELASTLOGIN).asOpt[Long]
        val creationDate  = (obj \ DATECREATED).asOpt[Long]
        val updateDate    = (obj \ DATEUPDATED).asOpt[Long]

        JsSuccess(
          MongoDBUser(
            id,
            nameLogin,
            password,
            accountType,
            lastLoginDate.map(new DateTime(_)),
            creationDate.map(new DateTime(_)),
            updateDate.map(new DateTime(_))))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsObject")
    }

    override def writes(user: MongoDBUser): JsObject = Json.obj(
      IDDB          -> user.id,
      NAMELOGIN     -> user.nameLogin,
      PASSWORD      -> user.password,
      ACCOUNTTYPE   -> user.accountType,
      DATELASTLOGIN -> user.lastLoginDate.fold(-1L)(_.getMillis),
      DATECREATED   -> user.creationDate.fold(-1L)(_.getMillis),
      DATEUPDATED   -> user.updateDate.fold(-1L)(_.getMillis))
  }

  /**
    * Form mapping for the Sign up form
    */
  val formSignUp = Form(
    mapping(
      ID            -> optional(text verifying pattern("""[a-fA-F0-9]{24}""".r, error = "error.objectId")),
      NAMELOGIN     -> text(6,40),
      PASSWORD      -> text(8,128),
      ACCEPTEDTOS   -> boolean,
      DATELASTLOGIN -> optional(longNumber),
      DATECREATED   -> optional(longNumber),
      DATEUPDATED   -> optional(longNumber)) {
      (id, nameLogin, password, acceptToS, lastLoginDate, creationDate, updateDate) =>
        MongoDBUser(
          id,
          nameLogin,
          BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS)),
          if (acceptToS) 1 else 0,
          lastLoginDate.map(new DateTime(_)),
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_))
        )
    } { user =>
      Some((
        user.id,
        user.nameLogin,
        "",
        true,
        user.lastLoginDate.map(_.getMillis),
        user.creationDate.map(_.getMillis),
        user.updateDate.map(_.getMillis)
        ))
    }
  )

  /**
    * Form mapping for the Sign in form
    */
  val formSignIn = Form(
    mapping(
      ID            -> optional(text verifying pattern("""[a-fA-F0-9]{24}""".r, error = "error.objectId")),
      NAMELOGIN     -> text(6,40),
      PASSWORD      -> text(8,128),
      DATELASTLOGIN -> optional(longNumber),
      DATECREATED   -> optional(longNumber),
      DATEUPDATED   -> optional(longNumber)) {
      (id, nameLogin, password, lastLoginDate, creationDate, updateDate) =>
        MongoDBUser(
          id,
          nameLogin,
          password,
          -1,
          lastLoginDate.map(new DateTime(_)),
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_))
        )
    } { user =>
      Some((
        user.id,
        user.nameLogin,
        "",
        user.lastLoginDate.map(_.getMillis),
        user.creationDate.map(_.getMillis),
        user.updateDate.map(_.getMillis)
        ))
    }
  )
}