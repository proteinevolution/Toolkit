package models.database

import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt

import reactivemongo.bson._

case class User(userID        : BSONObjectID,                        // ID of the User
                sessionID     : Option[BSONObjectID] = None,         // Session ID
                sessionData   : List[SessionData]    = List.empty,   // Session data separately from sid
                up            : Option[Boolean],
                accountType   : Int                  = -1,           // User Access level
                userData      : Option[UserData]     = None,         // Personal Data of the User //TODO possibly encrypt?
                userConfig    : UserConfig           = UserConfig(), // Configurable parts for the user
                jobs          : List[BSONObjectID]   = List.empty,   // List of Jobs the User has
                dateLastLogin : Option[DateTime],                    // Last seen on
                dateCreated   : Option[DateTime],                    // Account creation date
                dateUpdated   : Option[DateTime]) {                  // Account updated on

  def checkPassword(plainPassword: String) : Boolean = {
    BCrypt.checkpw(plainPassword, getUserData.password)
  }

  def getUserData = {
    // This should only return user data when the user is logged in.
    userData.getOrElse(UserData("invalid", "invalid", "invalid"))
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
  final val LOG_ROUNDS : Int = 10

  // Constants for the JSON object identifiers
  final val ID            = "id"                                // name for the ID in scala
  final val IDDB          = "_id"                               //              ID in MongoDB
  final val SESSIONID     = "sessionID"                         //              Session ID of the User
  final val SESSIONDATA   = "sessionData"                       //              session information
  final val UP            = "up"                                // is the user online?
  final val ACCOUNTTYPE   = "accountType"                       //              account type field
  final val USERDATA      = "userData"                          //              user data object field
  final val NAMELOGIN     = USERDATA + "." + UserData.NAMELOGIN //              login name field
  final val USERCONFIG    = "userConfig"
  final val JOBS          = "jobs"                              //              job reference pointers field
  final val ACCEPTEDTOS   = "acceptToS"                         // needed for checking if the TOS was accepted
  final val DATELASTLOGIN = "dateLastLogin"                     // name for the last login field
  final val DATECREATED   = "dateCreated"                       //              account created on field
  final val DATEUPDATED   = "dateUpdated"                       //              account data changed on field

  /**
    * Define how the User object is formatted
    */
  implicit object Reader extends BSONDocumentReader[User] {
    override def read(bson: BSONDocument): User = User(
      userID        = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID("None")),
      sessionID     = bson.getAs[BSONObjectID](SESSIONID),
      sessionData   = bson.getAs[List[SessionData]](SESSIONDATA).getOrElse(List.empty),
      up            = bson.getAs[Boolean](UP),
      accountType   = bson.getAs[BSONNumberLike](ACCOUNTTYPE).get.toInt,
      userData      = bson.getAs[UserData](USERDATA),
      userConfig    = bson.getAs[UserConfig](USERCONFIG).getOrElse(UserConfig()),
      jobs          = bson.getAs[List[BSONObjectID]](JOBS).getOrElse(List.empty),
      dateLastLogin = bson.getAs[BSONDateTime](DATELASTLOGIN).map(dt => new DateTime(dt.value)),
      dateCreated   = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
      dateUpdated   = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)))
  }

  implicit object Writer extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument = BSONDocument(
      IDDB          -> user.userID,
      SESSIONID     -> user.sessionID,
      SESSIONDATA   -> user.sessionData,
      UP            -> user.up,
      ACCOUNTTYPE   -> user.accountType,
      USERDATA      -> user.userData,
      USERCONFIG    -> user.userConfig,
      JOBS          -> user.jobs,
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