/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.user

import java.time.ZonedDateTime
import java.util.UUID

import de.proteinevolution.common.models.util.ZonedDateTimeHelper
import de.proteinevolution.user.AccountType.AccountType
import io.circe.syntax._
import io.circe.{ Encoder, Json }
import org.mindrot.jbcrypt.BCrypt
import reactivemongo.bson._

case class User(
    userID: String = UUID.randomUUID().toString,  // ID of the User
    sessionID: Option[String] = None,             // Session ID
    sessionData: List[SessionData] = List.empty,  // Session data separately from sid
    connected: Boolean = true,
    accountType: AccountType = AccountType.NORMALUSER, // User Access level
    userData: Option[UserData] = None,                 // Personal Data of the User //TODO possibly encrypt?
    userConfig: UserConfig = UserConfig(),             // Configurable parts for the user
    userToken: Option[UserToken] = None,
    jobs: List[String] = List.empty,                                // List of Jobs the User has
    dateDeletedOn: Option[ZonedDateTime] = None,                    // Date at which the account will be deleted on
    dateLastLogin: Option[ZonedDateTime] = Some(ZonedDateTime.now), // Last seen on
    dateCreated: Option[ZonedDateTime] = Some(ZonedDateTime.now),   // Account creation date
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
      case AccountType.ADMINLEVEL     => true
      case AccountType.MODERATORLEVEL => true
      case _                          => false
    }
  }

  def hasNotLoggedIn: Boolean = accountType == AccountType.CLOSETODELETIONUSER

  override def toString: String = {
    s"""userID: $userID
       |sessionID: ${sessionID.getOrElse("not logged in")}
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
  final val ID              = "id" // name for the ID in scala
  final val SESSION_ID      = "sessionID" //              Session ID of the User
  final val SESSION_DATA    = "sessionData" //              session information
  final val CONNECTED       = "connected" // is the user online?
  final val ACCOUNT_TYPE    = "accountType" //              account type field
  final val USER_DATA       = "userData" //              user data object field
  final val NAME_LOGIN      = s"$USER_DATA.${UserData.NAME_LOGIN}" //              login name field
  final val EMAIL           = s"$USER_DATA.${UserData.EMAIL}" //              email field
  final val PASSWORD        = s"$USER_DATA.${UserData.PASSWORD}" //              password field
  final val USER_CONFIG     = "userConfig"
  final val USER_TOKEN      = "userToken" //              token
  final val JOBS            = "jobs" //              job reference pointers field
  final val ACCEPTED_TOS    = "acceptToS" // needed for checking if the TOS was accepted
  final val DATE_LAST_LOGIN = "dateLastLogin" // name for the last login field
  final val DATE_DELETED_ON = "dateDeletedOn" // name for the field which holds the date when the account is going to be deleted
  final val DATE_CREATED    = "dateCreated" //              account created on field
  final val DATE_UPDATED    = "dateUpdated" //              account data changed on field

  implicit val encodeUser: Encoder[User] = (u: User) =>
    Json.obj(
      (ID, Json.fromString(u.userID)),
      (SESSION_ID, u.sessionID.map(id => Json.fromString(id)).getOrElse(Json.Null)),
      (SESSION_DATA, u.sessionData.asJson),
      (CONNECTED, Json.fromBoolean(u.connected)),
      (ACCOUNT_TYPE, Json.fromInt(u.accountType)),
      (UserData.NAME_LOGIN, Json.fromString(u.getUserData.nameLogin)),
      (UserData.EMAIL, Json.fromString(u.getUserData.eMail)),
      (JOBS, u.jobs.asJson),
      (
        DATE_LAST_LOGIN,
        u.dateLastLogin
          .map(zdt => Json.fromString(zdt.format(ZonedDateTimeHelper.dateTimeFormatter)))
          .getOrElse(Json.Null)
      ),
      (
        DATE_CREATED,
        u.dateCreated
          .map(zdt => Json.fromString(zdt.format(ZonedDateTimeHelper.dateTimeFormatter)))
          .getOrElse(Json.Null)
      ),
      (
        DATE_UPDATED,
        u.dateUpdated
          .map(zdt => Json.fromString(zdt.format(ZonedDateTimeHelper.dateTimeFormatter)))
          .getOrElse(Json.Null)
      )
    )

  implicit object Reader extends BSONDocumentReader[User] {
    override def read(bson: BSONDocument): User =
      User(
        userID = bson.getAs[String](ID).get,
        sessionID = bson.getAs[String](SESSION_ID),
        sessionData = bson.getAs[List[SessionData]](SESSION_DATA).getOrElse(List.empty),
        connected = bson.getAs[Boolean](CONNECTED).getOrElse(false),
        accountType = bson.getAs[BSONNumberLike](ACCOUNT_TYPE).get.toInt,
        userData = bson.getAs[UserData](USER_DATA),
        userConfig = bson.getAs[UserConfig](USER_CONFIG).getOrElse(UserConfig()),
        userToken = bson.getAs[UserToken](USER_TOKEN),
        jobs = bson.getAs[List[String]](JOBS).getOrElse(List.empty),
        dateDeletedOn = bson.getAs[BSONDateTime](DATE_DELETED_ON).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateLastLogin = bson.getAs[BSONDateTime](DATE_LAST_LOGIN).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateCreated = bson.getAs[BSONDateTime](DATE_CREATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateUpdated = bson.getAs[BSONDateTime](DATE_UPDATED).map(dt => ZonedDateTimeHelper.getZDT(dt))
      )
  }

  implicit object Writer extends BSONDocumentWriter[User] {
    override def write(user: User): BSONDocument =
      BSONDocument(
        ID            -> user.userID,
        SESSION_ID     -> user.sessionID,
        SESSION_DATA   -> user.sessionData,
        CONNECTED     -> user.connected,
        ACCOUNT_TYPE   -> user.accountType.toInt,
        USER_DATA      -> user.userData,
        USER_CONFIG    -> user.userConfig,
        USER_TOKEN     -> user.userToken,
        JOBS          -> user.jobs,
        DATE_DELETED_ON -> user.dateDeletedOn.map(dt => BSONDateTime(dt.toInstant.toEpochMilli)),
        DATE_LAST_LOGIN -> BSONDateTime(user.dateLastLogin.fold(-1L)(_.toInstant.toEpochMilli)),
        DATE_CREATED   -> BSONDateTime(user.dateCreated.fold(-1L)(_.toInstant.toEpochMilli)),
        DATE_UPDATED   -> BSONDateTime(user.dateUpdated.fold(-1L)(_.toInstant.toEpochMilli))
      )
  }

  final case class Login(nameLogin: String, password: String)

}
