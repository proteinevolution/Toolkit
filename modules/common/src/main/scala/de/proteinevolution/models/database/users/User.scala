package de.proteinevolution.models.database.users

import java.time.ZonedDateTime

import de.proteinevolution.models.util.ZonedDateTimeHelper
import io.circe.syntax._
import io.circe.{ Encoder, Json }
import org.mindrot.jbcrypt.BCrypt
import reactivemongo.bson._

case class User(
    userID: BSONObjectID = BSONObjectID.generate(), // ID of the User
    sessionID: Option[BSONObjectID] = None, // Session ID
    sessionData: List[SessionData] = List.empty, // Session data separately from sid
    connected: Boolean = true,
    accountType: Int = User.NORMALUSER, // User Access level
    userData: Option[UserData] = None, // Personal Data of the User //TODO possibly encrypt?
    userConfig: UserConfig = UserConfig(), // Configurable parts for the user
    userToken: Option[UserToken] = None,
    jobs: List[String] = List.empty, // List of Jobs the User has
    dateDeletedOn: Option[ZonedDateTime] = None, // Date at which the account will be deleted on
    dateLastLogin: Option[ZonedDateTime] = Some(ZonedDateTime.now), // Last seen on
    dateCreated: Option[ZonedDateTime] = Some(ZonedDateTime.now), // Account creation date
    dateUpdated: Option[ZonedDateTime] = Some(ZonedDateTime.now)
) { // Account updated on

  def checkPassword(plainPassword: String): Boolean = {
    BCrypt.checkpw(plainPassword, getUserData.password)
  }

  def getUserData: UserData = {
    // This should only return user data when the user is logged in.
    userData.getOrElse(UserData("invalid", "invalid", "invalid"))
  }

  // Mock up function to show how a possible function to check user levels could look like.
  def isSuperuser: Boolean = {
    accountType match {
      case User.ADMINLEVEL     => true
      case User.MODERATORLEVEL => true
      case _                   => false
    }
  }

  def hasNotLoggedIn: Boolean = accountType == 3

  override def toString: String = {
    s"""userID: ${userID.stringify}
       |sessionID: ${sessionID match {
         case Some(sid) => sid.stringify
         case None      => "not logged in"
       }}
       |connected: ${if (connected) "Yes" else "No"}
       |nameLogin: ${getUserData.nameLogin}
       |watched jobIDs: ${jobs.mkString(",")}
       |Deletion on: ${dateDeletedOn match {
         case Some(dateTime) => dateTime.toString
         case None           => "no deletion date set"
       }}""".stripMargin
  }
}

object User {
  // Number of rounds for BCrypt to hash the Password (2^x) TODO Move to the config?
  final val LOG_ROUNDS: Int = 10

  // Constants for the JSON object identifiers
  final val ID            = "id" // name for the ID in scala
  final val IDDB          = "_id" //              ID in MongoDB
  final val SESSIONID     = "sessionID" //              Session ID of the User
  final val SESSIONDATA   = "sessionData" //              session information
  final val CONNECTED     = "connected" // is the user online?
  final val ACCOUNTTYPE   = "accountType" //              account type field
  final val USERDATA      = "userData" //              user data object field
  final val NAMELOGIN     = s"$USERDATA.${UserData.NAMELOGIN}" //              login name field
  final val EMAIL         = s"$USERDATA.${UserData.EMAIL}" //              email field
  final val PASSWORD      = s"$USERDATA.${UserData.PASSWORD}" //              password field
  final val USERCONFIG    = "userConfig"
  final val USERTOKEN     = "userToken" //              token
  final val JOBS          = "jobs" //              job reference pointers field
  final val ACCEPTEDTOS   = "acceptToS" // needed for checking if the TOS was accepted
  final val DATELASTLOGIN = "dateLastLogin" // name for the last login field
  final val DATEDELETEDON = "dateDeletedOn" // name for the field which holds the date when the account is going to be deleted
  final val DATECREATED   = "dateCreated" //              account created on field
  final val DATEUPDATED   = "dateUpdated" //              account data changed on field

  final val ADMINLEVEL: Int                     = 11
  final val MODERATORLEVEL: Int                 = 10
  final val BANNEDUSER: Int                     = 4
  final val CLOSETODELETIONUSER: Int            = 3
  final val REGISTEREDUSER: Int                 = 1
  final val NORMALUSERAWAITINGREGISTRATION: Int = 0
  final val NORMALUSER: Int                     = -1

  implicit val encodeUser: Encoder[User] = (u: User) =>
    Json.obj(
      (ID, Json.fromString(u.userID.stringify)),
      (SESSIONID, u.sessionID.map(id => Json.fromString(id.stringify)).getOrElse(Json.Null)),
      (SESSIONDATA, u.sessionData.asJson),
      (CONNECTED, Json.fromBoolean(u.connected)),
      (ACCOUNTTYPE, Json.fromInt(u.accountType)),
      (UserData.NAMELOGIN, Json.fromString(u.getUserData.nameLogin)),
      (UserData.EMAIL, Json.fromString(u.getUserData.eMail)),
      (JOBS, u.jobs.asJson),
      (DATELASTLOGIN,
       u.dateLastLogin
         .map(zdt => Json.fromString(zdt.format(ZonedDateTimeHelper.dateTimeFormatter)))
         .getOrElse(Json.Null)),
      (DATECREATED,
       u.dateCreated
         .map(zdt => Json.fromString(zdt.format(ZonedDateTimeHelper.dateTimeFormatter)))
         .getOrElse(Json.Null)),
      (DATEUPDATED,
       u.dateUpdated
         .map(zdt => Json.fromString(zdt.format(ZonedDateTimeHelper.dateTimeFormatter)))
         .getOrElse(Json.Null))
  )

  implicit object Reader extends BSONDocumentReader[User] {
    override def read(bson: BSONDocument): User =
      User(
        userID = bson.getAs[BSONObjectID](IDDB).get,
        sessionID = bson.getAs[BSONObjectID](SESSIONID),
        sessionData = bson.getAs[List[SessionData]](SESSIONDATA).getOrElse(List.empty),
        connected = bson.getAs[Boolean](CONNECTED).getOrElse(false),
        accountType = bson.getAs[BSONNumberLike](ACCOUNTTYPE).get.toInt,
        userData = bson.getAs[UserData](USERDATA),
        userConfig = bson.getAs[UserConfig](USERCONFIG).getOrElse(UserConfig()),
        userToken = bson.getAs[UserToken](USERTOKEN),
        jobs = bson.getAs[List[String]](JOBS).getOrElse(List.empty),
        dateDeletedOn = bson.getAs[BSONDateTime](DATELASTLOGIN).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateLastLogin = bson.getAs[BSONDateTime](DATELASTLOGIN).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => ZonedDateTimeHelper.getZDT(dt))
      )
  }

  implicit object Writer extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument =
      BSONDocument(
        IDDB          -> user.userID,
        SESSIONID     -> user.sessionID,
        SESSIONDATA   -> user.sessionData,
        CONNECTED     -> user.connected,
        ACCOUNTTYPE   -> user.accountType,
        USERDATA      -> user.userData,
        USERCONFIG    -> user.userConfig,
        USERTOKEN     -> user.userToken,
        JOBS          -> user.jobs,
        DATEDELETEDON -> user.dateDeletedOn.map(dt => BSONDateTime(dt.toInstant.toEpochMilli)),
        DATELASTLOGIN -> BSONDateTime(user.dateLastLogin.fold(-1L)(_.toInstant.toEpochMilli)),
        DATECREATED   -> BSONDateTime(user.dateCreated.fold(-1L)(_.toInstant.toEpochMilli)),
        DATEUPDATED   -> BSONDateTime(user.dateUpdated.fold(-1L)(_.toInstant.toEpochMilli))
      )
  }

  final case class Login(nameLogin: String, password: String)

}
