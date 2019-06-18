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

package de.proteinevolution.auth.dao

import java.time.ZonedDateTime
import java.util.UUID

import de.proteinevolution.user._
import javax.inject.{Inject, Singleton}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{UpdateWriteResult, WriteResult}
import reactivemongo.bson.{BSONArray, BSONDateTime, BSONDocument}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserDao @Inject()(private val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  private[auth] lazy val userCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))
  }

  def addUser(user: User): Future[WriteResult] = userCollection.flatMap(_.insert(ordered = false).one(user))

  def findUserByUsername(username: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.NAME_LOGIN -> username), None).one[User])

  def findUserByEmail(email: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.EMAIL -> email), None).one[User])

  def findUserByUsernameOrEmail(username: String, email: String): Future[Option[User]] =
    userCollection.flatMap(
      _.find(
        BSONDocument("$or" -> List(BSONDocument(User.EMAIL -> email), BSONDocument(User.NAME_LOGIN -> username))),
        None
      ).one[User]
    )

  def findUserBySessionId(sessionID: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.SESSION_ID -> sessionID), None).one[User])

  def findUserByID(userID: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.ID -> userID), None).one[User])

  def findUsers(selector: BSONDocument): Future[scala.List[User]] =
    userCollection
      .map(_.find(selector, None).cursor[User]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[User]]()))

  /**
   * modify the user internally. Do not expose any db logic to the outside.
   *
   * @param userID   identifier of the user
   * @param modifier operations to perform on the user
   * @return
   */
  private def modifyUser(userID: String, modifier: BSONDocument): Future[Option[User]] = {
    val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
    userCollection.flatMap(
      _.findAndUpdate(
        BSONDocument(User.ID -> userID),
        modifier.merge(
          BSONDocument(
            "$set" ->
            BSONDocument(User.DATE_UPDATED -> bsonCurrentTime)
          )
        ),
        fetchNewObject = true
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
      ).merge {
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
          User.PASSWORD  -> newPasswordHash,
          User.SESSION_ID -> newSessionId
        )
      ).merge {
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
          User.DATE_LAST_LOGIN -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli),
          User.SESSION_ID     -> user.sessionID.orElse(Some(UUID.randomUUID().toString)) // user needs session id
        )
      ).merge(
          // In the case that the user has been emailed about their inactivity, reset that status to a regular user status
          if (user.accountType == AccountType.CLOSETODELETIONUSER) {
            BSONDocument(
              "$set"   -> BSONDocument(User.ACCOUNT_TYPE   -> AccountType.REGISTEREDUSER.toInt),
              "$unset" -> BSONDocument(User.DATE_DELETED_ON -> "")
            )
          } else {
            BSONDocument.empty
          }
        )
        .merge(
          sessionDataOption
            .map(
              sessionData =>
                // Add the session Data to the set
                BSONDocument("$addToSet" -> BSONDocument(User.SESSION_DATA -> sessionData))
            )
            .getOrElse(BSONDocument.empty)
        )
    )

  def afterRemoveFromCache(userID: String): Future[Option[User]] =
    modifyUser(userID, BSONDocument(
      "$set" ->
        BSONDocument(
          User.DATE_LAST_LOGIN -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
        ),
      "$unset" ->
        BSONDocument(
          User.SESSION_ID -> "",
          User.CONNECTED -> ""
        )
    ))

  def registerForDeletion(userIDs: List[String], deletionDateMillis: Long): Future[WriteResult] =
    userCollection.flatMap(
      _.update(ordered = false).one(BSONDocument(User.ID -> BSONDocument("$in" -> userIDs)), BSONDocument(
        "$set" ->
          BSONDocument(
            User.ACCOUNT_TYPE   -> AccountType.CLOSETODELETIONUSER.toInt,
            User.DATE_DELETED_ON -> BSONDateTime(deletionDateMillis)
          )
      ), multi = true)
    )

  def removeUsers(userIDs: List[String]): Future[WriteResult] =
    userCollection.flatMap(_.delete().one(BSONDocument(User.ID -> BSONDocument("$in" -> userIDs))))

  def upsertUser(user: User): Future[Option[User]] =
    userCollection.flatMap(
      _.findAndUpdate(
        selector = BSONDocument(User.ID -> user.userID),
        update = user,
        upsert = true,
        fetchNewObject = true
      ).map(_.result[User])
    )

  /* removes job association from user */
  def removeJob(jobID: String): Future[UpdateWriteResult] =
    userCollection.flatMap {
      _.update(ordered = false).one(
        BSONDocument.empty,
        BSONDocument("$pull" -> BSONDocument("jobs" -> BSONDocument("$in" -> BSONArray(jobID)))),
        multi = true
      )
    }

}
