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

import de.proteinevolution.user.{ User, UserData }
import javax.inject.{ Inject, Singleton }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.{ UpdateWriteResult, WriteResult }
import reactivemongo.bson.{ BSONArray, BSONDateTime, BSONDocument, BSONObjectID }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class UserDao @Inject()(private val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  private[auth] lazy val userCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))
  }

  def addUser(user: User): Future[WriteResult] = userCollection.flatMap(_.insert(ordered = false).one(user))

  def findUserByUsername(username: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.NAMELOGIN -> username), None).one[User])

  def findUserByEmail(email: String): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.EMAIL -> email), None).one[User])

  def findUserByUsernameOrEmail(username: String, email: String): Future[Option[User]] =
    userCollection.flatMap(
      _.find(
        BSONDocument("$or" -> List(BSONDocument(User.EMAIL -> email), BSONDocument(User.NAMELOGIN -> username))),
        None
      ).one[User]
    )

  def findUserBySessionId(sessionID: BSONObjectID): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.SESSIONID -> sessionID), None).one[User])

  def findUserByDBID(dbID: BSONObjectID): Future[Option[User]] =
    userCollection.flatMap(_.find(BSONDocument(User.IDDB -> dbID), None).one[User])

  def findUsers(selector: BSONDocument): Future[scala.List[User]] =
    userCollection
      .map(_.find(selector, None).cursor[User]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[User]]()))

  /**
   * @deprecated very bad practice to have db logic in controllers. Should be private
   */
  @Deprecated
  def modifyUser(userID: BSONObjectID, modifier: BSONDocument): Future[Option[User]] =
    userCollection.flatMap(
      _.findAndUpdate(BSONDocument(User.IDDB -> userID), modifier, fetchNewObject = true).map(_.result[User])
    )

  def updateAccountType(userID: BSONObjectID, accountType: Int, resetUserToken: Boolean = false) = {
    var modifier = BSONDocument(      "$set" ->
        BSONDocument(
          User.ACCOUNTTYPE -> accountType,
          User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
        )    )
    if (resetUserToken) {
      modifier = modifier ++ BSONDocument(
        "$unset" ->
          BSONDocument(User.USERTOKEN -> "")
      )
    }
    modifyUser(userID, modifier)
  }

  def changePassword(
      userID: BSONObjectID,
      newPasswordHash: String,
      newSessionId: BSONObjectID
  ): Future[Option[User]] = {
    // create a modifier document to change the last login date in the Database
    val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
    // Push to the database using modifier
    val modifier =
      BSONDocument(
        "$set" -> BSONDocument(
          User.DATEUPDATED -> bsonCurrentTime,
          User.PASSWORD    -> newPasswordHash,
          User.SESSIONID   -> newSessionId
        )
      )
    modifyUser(userID, modifier)
  }

  def updateUserData(userID: BSONObjectID, userData: UserData): Future[Option[User]] = {
    // create a modifier document to change the last login date in the Database
    val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
    val modifier = BSONDocument(
      "$set" ->
      BSONDocument(
        User.USERDATA      -> userData,
        User.DATELASTLOGIN -> bsonCurrentTime,
        User.DATEUPDATED   -> bsonCurrentTime
      )
    )
    modifyUser(userID, modifier)
  }

  /**
   * @deprecated very bad practice to have db logic in controllers.
   */
  @Deprecated
  def modifyUsers(userIDs: List[BSONObjectID], modifier: BSONDocument): Future[WriteResult] =
    userCollection.flatMap(
      _.update(ordered = false).one(BSONDocument(User.IDDB -> BSONDocument("$in" -> userIDs)), modifier, multi = true)
    )

  def removeUsers(userIDs: List[BSONObjectID]): Future[WriteResult] =
    userCollection.flatMap(_.delete().one(BSONDocument(User.IDDB -> BSONDocument("$in" -> userIDs))))

  def upsertUser(user: User): Future[Option[User]] =
    userCollection.flatMap(
      _.findAndUpdate(
        selector = BSONDocument(User.IDDB -> user.userID),
        update = user,
        upsert = true,
        fetchNewObject = true
      ).map(_.result[User])
    )

  /* removes job association from user */
  def removeJob(jobId: String): Future[UpdateWriteResult] =
    userCollection.flatMap {
      _.update(ordered = false).one(
        BSONDocument.empty,
        BSONDocument("$pull" -> BSONDocument("jobs" -> BSONDocument("$in" -> BSONArray(jobId)))),
        multi = true
      )
    }

}
