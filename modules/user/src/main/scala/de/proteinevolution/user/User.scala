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
import reactivemongo.api.bson._

case class User(
    userID: String = UUID.randomUUID().toString, // ID of the User
    sessionID: Option[String] = None,            // Session ID
    sessionData: List[SessionData] = List.empty, // Session data separately from sid
    connected: Boolean = true,
    accountType: AccountType = AccountType.NORMALUSER, // User Access level
    userData: Option[UserData] = None,                 // Personal Data of the User //TODO possibly encrypt?
    userConfig: UserConfig = UserConfig(),             // Configurable parts for the user
    userToken: Option[UserToken] = None,
    jobs: List[String] = List.empty,                  // List of Jobs the User is watching (counterpart to job.watchList)
    deletionWarningSent: Boolean = false,             // keep track if the mail was sent already
    dateLastLogin: ZonedDateTime = ZonedDateTime.now, // Last seen on
    dateCreated: ZonedDateTime = ZonedDateTime.now,   // Account creation date
    dateUpdated: ZonedDateTime = ZonedDateTime.now
) { // Account updated on

  def checkPassword(plainPassword: String): Boolean = {
    BCrypt.checkpw(plainPassword, userData.map(_.password).getOrElse(""))
  }

  def getUserDataWithAdmin: Option[UserData] = userData.map(_.copy(isAdmin = isSuperuser))

  def isRegistered: Boolean = userData.isDefined

  // Mock up function to show how a possible function to check user levels could look like.
  def isSuperuser: Boolean = {
    accountType match {
      case AccountType.ADMINLEVEL     => true
      case AccountType.MODERATORLEVEL => true
      case _                          => false
    }
  }

  override def toString: String = {
    s"""userID: $userID
       |sessionID: ${sessionID.getOrElse("not logged in")}
       |connected: ${if (connected) "Yes" else "No"}
       |nameLogin: ${userData.map(_.nameLogin)}
       |watched jobIDs: ${jobs.mkString(",")}""".stripMargin
  }
}

object User {
  // Number of rounds for BCrypt to hash the Password (2^x) TODO Move to the config?
  final val LOG_ROUNDS: Int = 10

  // Constants for the JSON object identifiers
  final val ID                    = "id"                                 // name for the ID in scala
  final val SESSION_ID            = "sessionID"                          // Session ID of the User
  final val SESSION_DATA          = "sessionData"                        // session information
  final val CONNECTED             = "connected"                          // is the user online?
  final val ACCOUNT_TYPE          = "accountType"                        // account type field
  final val USER_DATA             = "userData"                           // user data object field
  final val NAME_LOGIN            = s"$USER_DATA.${UserData.NAME_LOGIN}" // login name field
  final val EMAIL                 = s"$USER_DATA.${UserData.EMAIL}"      // email field
  final val PASSWORD              = s"$USER_DATA.${UserData.PASSWORD}"   // password field
  final val USER_CONFIG           = "userConfig"
  final val USER_TOKEN            = "userToken"                          // token
  final val JOBS                  = "jobs"                               // job reference pointers field
  final val ACCEPTED_TOS          = "acceptToS"                          // needed for checking if the TOS was accepted
  final val DELETION_WARNING_SENT = "deletionWarningSent"                // make sure not to send mail twice
  final val DATE_LAST_LOGIN       = "dateLastLogin"                      // name for the last login field
  final val DATE_CREATED          = "dateCreated"                        // account created on field
  final val DATE_UPDATED          = "dateUpdated"                        // account data changed on field

  implicit val encodeUser: Encoder[User] = (u: User) =>
    Json.obj(
      (ID, Json.fromString(u.userID)),
      (SESSION_ID, u.sessionID.map(id => Json.fromString(id)).getOrElse(Json.Null)),
      (SESSION_DATA, u.sessionData.asJson),
      (CONNECTED, Json.fromBoolean(u.connected)),
      (ACCOUNT_TYPE, Json.fromInt(u.accountType)),
      (UserData.NAME_LOGIN, Json.fromString(u.userData.map(_.nameLogin).getOrElse(Json.Null))),
      (UserData.EMAIL, Json.fromString(u.userData.map(_.eMail).getOrElse(Json.Null))),
      (JOBS, u.jobs.asJson),
      (DATE_LAST_LOGIN, Json.fromString(u.dateLastLogin.format(ZonedDateTimeHelper.dateTimeFormatter))),
      (DATE_CREATED, Json.fromString(u.dateCreated.format(ZonedDateTimeHelper.dateTimeFormatter))),
      (DATE_UPDATED, Json.fromString(u.dateUpdated.format(ZonedDateTimeHelper.dateTimeFormatter)))
    )

  implicit def reader: BSONDocumentReader[User] =
    BSONDocumentReader[User] { bson =>
      User(
        userID = bson.getAsOpt[String](ID).get,
        sessionID = bson.getAsOpt[String](SESSION_ID),
        sessionData = bson.getAsOpt[List[SessionData]](SESSION_DATA).getOrElse(List.empty),
        connected = bson.getAsOpt[Boolean](CONNECTED).getOrElse(false),
        accountType = bson
          .getAsTry[BSONNumberLike](ACCOUNT_TYPE)
          .flatMap(_.toInt)
          .getOrElse(AccountType.NORMALUSERAWAITINGREGISTRATION),
        userData = bson.getAsOpt[UserData](USER_DATA),
        userConfig = bson.getAsOpt[UserConfig](USER_CONFIG).getOrElse(UserConfig()),
        userToken = bson.getAsOpt[UserToken](USER_TOKEN),
        jobs = bson.getAsOpt[List[String]](JOBS).getOrElse(List.empty),
        deletionWarningSent = bson.getAsOpt[Boolean](DELETION_WARNING_SENT).getOrElse(false),
        dateLastLogin = bson.getAsOpt[BSONDateTime](DATE_LAST_LOGIN).map(ZonedDateTimeHelper.getZDT).get,
        dateCreated = bson.getAsOpt[BSONDateTime](DATE_CREATED).map(ZonedDateTimeHelper.getZDT).get,
        dateUpdated = bson.getAsOpt[BSONDateTime](DATE_UPDATED).map(ZonedDateTimeHelper.getZDT).get
      )
    }

  implicit def writer: BSONDocumentWriter[User] =
    BSONDocumentWriter[User] { user =>
      BSONDocument(
        ID                    -> user.userID,
        SESSION_ID            -> user.sessionID,
        SESSION_DATA          -> user.sessionData,
        CONNECTED             -> user.connected,
        ACCOUNT_TYPE          -> user.accountType.toInt,
        USER_DATA             -> user.userData,
        USER_CONFIG           -> user.userConfig,
        USER_TOKEN            -> user.userToken,
        JOBS                  -> user.jobs,
        DELETION_WARNING_SENT -> user.deletionWarningSent,
        DATE_LAST_LOGIN       -> BSONDateTime(user.dateLastLogin.toInstant.toEpochMilli),
        DATE_CREATED          -> BSONDateTime(user.dateCreated.toInstant.toEpochMilli),
        DATE_UPDATED          -> BSONDateTime(user.dateUpdated.toInstant.toEpochMilli)
      )
    }

  final case class Login(nameLogin: String, password: String)

}
