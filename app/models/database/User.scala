package models.database

import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt

import play.api.data._
import play.api.data.Forms.{ text, longNumber, mapping, boolean, email, optional }
import play.api.data.validation.Constraints.pattern
import reactivemongo.bson._

case class User(userID        : BSONObjectID,           // ID of the User
                nameLogin     : String,                 // Login name of the User
                password      : String,                 // Password of the User (Hashed)
                accountType   : Int,                    // User Access level
                userData      : UserData,               // Personal Data of the User //TODO possibly encrypt?
                jobs          : List[BSONObjectID],     // List of Jobs the User has
                dateLastLogin : Option[DateTime],       // Last seen on
                dateCreated   : Option[DateTime],       // Account creation date
                dateUpdated   : Option[DateTime]) {     // Account updated on

  def checkPassword(plainPassword: String) : Boolean = {
    BCrypt.checkpw(plainPassword, password)
  }

  // Mock up function to show how a possible function to check user levels could look like.
  def isSuperuser : Boolean = {
    accountType match {
      case 10 => true
      case 11 => true
      case _  => false
    }
  }
}

case class Login(nameLogin : String, password : String)

object User {
  // Number of rounds for BCrypt to hash the Password (2^x) TODO Move to the config?
  val LOG_ROUNDS : Int = 10

  // Constants for the JSON object identifiers
  val ID            = "id"            // name for the ID in scala
  val IDDB          = "_id"           //              ID in MongoDB
  val NAMELOGIN     = "nameLogin"     //              login name field
  val PASSWORD      = "password"      //              password field
  val ACCOUNTTYPE   = "accountType"   //              account type field
  val USERDATA      = "userData"      //              user data object field
  val JOBS          = "jobs"          //              job reference pointers field
  val ACCEPTEDTOS   = "acceptToS"     // needed for checking if the TOS was accepted
  val DATELASTLOGIN = "dateLastLogin" // name for the last login field
  val DATECREATED   = "dateCreated"   //              account created on field
  val DATEUPDATED   = "dateUpdated"   //              account data changed on field

  /**
    * Define how the User object is formatted
    */
  implicit object Reader extends BSONDocumentReader[User] {
    override def read(bson: BSONDocument): User = User(
      userID        = bson.getAs[BSONObjectID](IDDB).get,
      nameLogin     = bson.getAs[String](NAMELOGIN).get,
      password      = bson.getAs[String](PASSWORD).get,
      accountType   = bson.getAs[BSONNumberLike](ACCOUNTTYPE).get.toInt,
      userData      = bson.getAs[UserData](USERDATA).get,
      jobs          = bson.getAs[List[BSONObjectID]](JOBS).get,
      dateLastLogin = bson.getAs[BSONDateTime](DATELASTLOGIN).map(dt => new DateTime(dt.value)),
      dateCreated   = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
      dateUpdated   = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)))
  }

  implicit object Writer extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument = BSONDocument(
      IDDB          -> user.userID,
      NAMELOGIN     -> user.nameLogin,
      PASSWORD      -> user.password,
      ACCOUNTTYPE   -> user.accountType,
      USERDATA      -> user.userData,
      JOBS          -> BSONArray(user.jobs),
      DATELASTLOGIN -> BSONDateTime(user.dateLastLogin.fold(-1L)(_.getMillis)),
      DATECREATED   -> BSONDateTime(user.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED   -> BSONDateTime(user.dateUpdated.fold(-1L)(_.getMillis)))
  }

  /**
    * Form mapping for the Sign up form
    */
  val formSignUp = Form(
    mapping(
      NAMELOGIN      -> (text(6,40) verifying pattern("""[^\\"\\(\\)\\[\\]]*""".r, error = "error.objectId")),
      PASSWORD       -> (text(8,128) verifying pattern("""[^\\"\\(\\)\\[\\]]*""".r, error = "error.objectId")),
      UserData.EMAIL -> email,
      ACCEPTEDTOS    -> boolean,
      DATELASTLOGIN  -> optional(longNumber),
      DATECREATED    -> optional(longNumber),
      DATEUPDATED    -> optional(longNumber)) {
      (nameLogin, password, eMail, acceptToS, dateLastLogin, dateCreated, dateUpdated) =>
        User(
          userID        = BSONObjectID.generate,
          nameLogin     = nameLogin,
          password      = BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS)),
          accountType   = if (acceptToS) 1 else 0,
          userData      = UserData(None,
                                   None,
                                   eMail = eMail,
                                   None,
                                   None,
                                   None,
                                   None,
                                   None,
                                   None),
          jobs          = Nil,
          dateLastLogin = Some(new DateTime()),
          dateCreated   = Some(new DateTime()),
          dateUpdated   = Some(new DateTime())
        )
    } { user =>
      Some((
        user.nameLogin,
        "",
        user.userData.eMail,
        true,
        user.dateLastLogin.map(_.getMillis),
        user.dateCreated.map(_.getMillis),
        user.dateUpdated.map(_.getMillis)
        ))
    }
  )

  /**
    * Form mapping for the Sign in form
    */
  val formSignIn = Form(
    mapping(
      NAMELOGIN     -> text(6,40),
      PASSWORD      -> text(8,128)) {
      (nameLogin, password) =>
        Login(
          nameLogin,
          password
        )
    } { user =>
      Some((
        user.nameLogin,
        ""
        ))
    }
  )
}