/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

package de.proteinevolution.auth.dao

import java.time.ZonedDateTime
import java.util.UUID

import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.user._
import javax.inject.{ Inject, Singleton }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{ BSONArray, BSONDateTime, BSONDocument }
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{ Cursor, WriteConcern }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class UserDao @Inject() (
    private val reactiveMongoApi: ReactiveMongoApi,
    constants: ConstantsV2
)(implicit ec: ExecutionContext) {

  private[auth] lazy val userCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))
  }

  def addUser(user: User): Future[WriteResult] = userCollection.flatMap(_.insert(ordered = false).one(user))

  def findUserByUsername(username: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.NAME_LOGIN -> username), Option.empty[BSONDocument]).one[User])

  def findUserByEmail(email: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.EMAIL -> email), Option.empty[BSONDocument]).one[User])

  def findUserByUsernameOrEmail(username: String, email: String): Future[Option[User]] =
    userCollection.flatMap(
      _.find(
        BSONDocument("$or" -> List(BSONDocument(User.EMAIL -> email), BSONDocument(User.NAME_LOGIN -> username))),
        Option.empty[BSONDocument]
      ).one[User]
    )

  def findUserBySessionId(sessionID: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.SESSION_ID -> sessionID), Option.empty[BSONDocument]).one[User])

  def findUserByID(userID: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.ID -> userID), Option.empty[BSONDocument]).one[User])

  def findOldUsers(): Future[List[User]] = {
    // Generate the dates to compare before deletion
    val now = ZonedDateTime.now
    // Date to compare last login of regular users to
    val deleteRegularUserAfterCreationDate = now.minusMonths(constants.userDeleting.toLong)
    // date to compare creation date of unconfirmed users to
    val deleteUnconfirmedUserAfterCreationDate = now.minusDays(constants.userDeletingRegisterEmail.toLong)
    // date to compare last login of registered users to
    val deleteRegistratedUserAfterLoginDate = now.minusMonths(constants.userDeletingRegistered.toLong)
    internalFindUsers(
      BSONDocument(
        "$or" ->
        List(
          BSONDocument( // Removing regular users with no privileges
            User.ACCOUNT_TYPE ->
            AccountType.NORMALUSER.toInt,
            User.DATE_CREATED ->
            BSONDocument("$lt" -> BSONDateTime(deleteRegularUserAfterCreationDate.toInstant.toEpochMilli))
          ),
          BSONDocument( // Removing regular users who await registration
            User.ACCOUNT_TYPE ->
            AccountType.NORMALUSERAWAITINGREGISTRATION.toInt,
            User.DATE_CREATED ->
            BSONDocument("$lt" -> BSONDateTime(deleteUnconfirmedUserAfterCreationDate.toInstant.toEpochMilli))
          ),
          BSONDocument( // Removing registered users with no privileges
            User.ACCOUNT_TYPE ->
            AccountType.REGISTEREDUSER.toInt,
            User.DATE_LAST_LOGIN ->
            BSONDocument("$lt" -> BSONDateTime(deleteRegistratedUserAfterLoginDate.toInstant.toEpochMilli))
          )
        )
      )
    )
  }

  def findUsersToWarn(): Future[List[User]] = {
    val now = ZonedDateTime.now
    // Date to warn registered accounts that they will soon be deleted
    val warnRegistratedUserAfterLoginDate =
      now.minusDays(constants.userDeletionWarning.toLong).toLocalDate.atStartOfDay(now.getZone)
    internalFindUsers(
      BSONDocument(
        User.ACCOUNT_TYPE          -> AccountType.REGISTEREDUSER.toInt,
        User.DELETION_WARNING_SENT -> false,
        User.CONNECTED             -> false,
        User.DATE_LAST_LOGIN -> BSONDocument(
          "$lt" -> BSONDateTime(warnRegistratedUserAfterLoginDate.toInstant.toEpochMilli)
        )
      )
    )
  }

  def findUsersWithInformation(): Future[List[User]] =
    internalFindUsers(BSONDocument(User.USER_DATA -> BSONDocument("$exists" -> true)))

  private def internalFindUsers(selector: BSONDocument): Future[scala.List[User]] =
    userCollection
      .map(_.find(selector, Option.empty[BSONDocument]).cursor[User]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[User]]()))

  /**
   * modify the user internally. Do not expose any db logic to the outside.
   *
   * @param userID
   *   identifier of the user
   * @param modifier
   *   operations to perform on the user
   * @return
   */
  private def modifyUser(userID: String, modifier: BSONDocument): Future[Option[User]] = {
    val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
    userCollection.flatMap(
      _.findAndUpdate(
        BSONDocument(User.ID -> userID),
        modifier ++ BSONDocument(
          "$set" ->
          BSONDocument(User.DATE_UPDATED -> bsonCurrentTime)
        ),
        fetchNewObject = true,
        // the following values are default values that are used to distinguish findAndUpdate from deprecated version
        // TODO: why won't it accept it with values left out like in documentation
        upsert = false,
        None,
        None,
        bypassDocumentValidation = false,
        WriteConcern.Default,
        Option.empty,
        Option.empty,
        Seq.empty
      ).map(_.result[User])
    )
  }

  def setToken(userID: String, token: UserToken): Future[Option[User]] =
    modifyUser(
      userID,
      BSONDocument(
        "$set" ->
        BSONDocument(User.USER_TOKEN -> token)
      )
    )

  def setDeletionWarningSent(userID: String): Future[Option[User]] =
    modifyUser(userID, BSONDocument("$set" -> BSONDocument(User.DELETION_WARNING_SENT -> true)))

  def updateAccountType(
      userID: String,
      accountType: Int,
      resetUserToken: Boolean = false
  ): Future[Option[User]] =
    modifyUser(
      userID,
      BSONDocument(
        "$set" ->
        BSONDocument(User.ACCOUNT_TYPE -> accountType)
      ) ++ {
        if (resetUserToken) {
          BSONDocument("$unset" -> BSONDocument(User.USER_TOKEN -> ""))
        } else {
          BSONDocument.empty
        }
      }
    )

  def changePassword(
      userID: String,
      newPasswordHash: String,
      newSessionId: String,
      resetUserToken: Boolean = false
  ): Future[Option[User]] =
    modifyUser(
      userID,
      BSONDocument(
        "$set" -> BSONDocument(
          User.PASSWORD   -> newPasswordHash,
          User.SESSION_ID -> newSessionId
        )
      ) ++ {
        if (resetUserToken) {
          BSONDocument("$unset" -> BSONDocument(User.USER_TOKEN -> ""))
        } else {
          BSONDocument.empty
        }
      }
    )

  def updateUserData(userID: String, userData: UserData): Future[Option[User]] =
    modifyUser(
      userID,
      BSONDocument(
        "$set" ->
        BSONDocument(
          User.USER_DATA -> userData
        )
      )
    )

  def updateUserConfig(userID: String, userConfig: UserConfig): Future[Option[User]] =
    modifyUser(
      userID,
      BSONDocument(
        "$set" ->
        BSONDocument(
          User.USER_CONFIG -> userConfig
        )
      )
    )

  def addJobsToUser(userID: String, jobs: List[String]): Future[Option[User]] =
    modifyUser(
      userID,
      BSONDocument(
        "$addToSet" ->
        BSONDocument(
          User.JOBS -> BSONDocument("$each" -> jobs)
        )
      )
    )

  def removeJobsFromUser(userID: String, jobs: List[String]): Future[Option[User]] =
    modifyUser(
      userID,
      BSONDocument(
        "$pullAll" -> BSONDocument(User.JOBS -> BSONArray(jobs))
      )
    )

  def saveNewLogin(user: User, sessionDataOption: Option[SessionData] = None): Future[Option[User]] =
    modifyUser(
      user.userID,
      BSONDocument(
        "$set" -> BSONDocument(
          User.DATE_LAST_LOGIN       -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli),
          User.DELETION_WARNING_SENT -> false,
          User.CONNECTED             -> true,
          User.SESSION_ID            -> user.sessionID.orElse(Some(UUID.randomUUID().toString)) // user needs session id
        )
      ) ++ sessionDataOption
        .map(sessionData =>
          // Add the session Data to the set
          BSONDocument("$addToSet" -> BSONDocument(User.SESSION_DATA -> sessionData))
        )
        .getOrElse(BSONDocument.empty)
    )

  def afterRemoveFromCache(userID: String): Future[Option[User]] =
    modifyUser(
      userID,
      BSONDocument(
        "$set" ->
        BSONDocument(
          User.DATE_LAST_LOGIN -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
        ),
        "$unset" ->
        BSONDocument(
          User.SESSION_ID -> "",
          User.CONNECTED  -> false
        )
      )
    )

  def removeUsers(userIDs: List[String]): Future[WriteResult] =
    userCollection.flatMap(_.delete().one(BSONDocument(User.ID -> BSONDocument("$in" -> userIDs))))

  def upsertUser(user: User): Future[Option[User]] =
    userCollection.flatMap(
      _.findAndUpdate(
        BSONDocument(User.ID -> user.userID),
        user,
        fetchNewObject = true,
        upsert = true,
        // the following values are default values that are used to distinguish findAndUpdate from deprecated version
        // TODO: why won't it accept it with values left out like in documentation
        None,
        None,
        bypassDocumentValidation = false,
        WriteConcern.Default,
        Option.empty,
        Option.empty,
        Seq.empty
      ).map(_.result[User])
    )

  /* removes job association from user */
  def removeJobs(jobIDs: List[String]): Future[WriteResult] =
    userCollection.flatMap {
      _.update(ordered = false).one(
        BSONDocument.empty,
        BSONDocument("$pull" -> BSONDocument("jobs" -> BSONDocument("$in" -> jobIDs))),
        multi = true
      )
    }

}
