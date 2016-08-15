package models.database

import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt

import reactivemongo.bson._

case class User(userID        : BSONObjectID,           // ID of the User
                nameLogin     : Option[String],         // Login name of the User
                accountType   : Int,                    // User Access level
                userData      : Option[UserData],       // Personal Data of the User //TODO possibly encrypt?
                jobs          : List[BSONObjectID],     // List of Jobs the User has
                dateLastLogin : Option[DateTime],       // Last seen on
                dateCreated   : Option[DateTime],       // Account creation date
                dateUpdated   : Option[DateTime]) {     // Account updated on

  def checkPassword(plainPassword: String) : Boolean = {
    BCrypt.checkpw(plainPassword, getUserData.password)
  }

  def getUserData = {
    userData.getOrElse(UserData("invalid", "invalid", None, None, None, None, None, None, None, None))
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
      nameLogin     = bson.getAs[String](NAMELOGIN),
      accountType   = bson.getAs[BSONNumberLike](ACCOUNTTYPE).get.toInt,
      userData      = bson.getAs[UserData](USERDATA),
      jobs          = bson.getAs[List[BSONObjectID]](JOBS).get,
      dateLastLogin = bson.getAs[BSONDateTime](DATELASTLOGIN).map(dt => new DateTime(dt.value)),
      dateCreated   = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
      dateUpdated   = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)))
  }

  implicit object Writer extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument = BSONDocument(
      IDDB          -> user.userID,
      NAMELOGIN     -> user.nameLogin,
      ACCOUNTTYPE   -> user.accountType,
      USERDATA      -> user.userData,
      JOBS          -> BSONArray(user.jobs),
      DATELASTLOGIN -> BSONDateTime(user.dateLastLogin.fold(-1L)(_.getMillis)),
      DATECREATED   -> BSONDateTime(user.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED   -> BSONDateTime(user.dateUpdated.fold(-1L)(_.getMillis)))
  }


  /**
    * Helper class for a login Form Object
    *
    * @param nameLogin
    * @param password
    */
  case class Login(nameLogin : String, password : String)
}