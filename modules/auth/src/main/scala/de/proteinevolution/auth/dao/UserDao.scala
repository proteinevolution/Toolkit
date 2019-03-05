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

import de.proteinevolution.user.User
import javax.inject.{ Inject, Singleton }
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class UserDao @Inject()(private val reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext) {

  private[auth] lazy val userCollection: Future[BSONCollection] = {
    reactiveMongoApi.database.map(_.collection[BSONCollection]("users"))
  }

  def addUser(user: User): Future[WriteResult] = userCollection.flatMap(_.insert(user))

  def findUser(selector: BSONDocument): Future[Option[User]] =
    userCollection.flatMap(_.find(selector, None).one[User])

  def findUsers(selector: BSONDocument): Future[scala.List[User]] = {
    userCollection
      .map(_.find(selector, None).cursor[User]())
      .flatMap(_.collect[List](-1, Cursor.FailOnError[List[User]]()))
  }

  def modifyUser(selector: BSONDocument, modifier: BSONDocument): Future[Option[User]] = {
    userCollection.flatMap(_.findAndUpdate(selector, modifier, fetchNewObject = true).map(_.result[User]))
  }

  def modifyUsers(selector: BSONDocument, modifier: BSONDocument): Future[WriteResult] = {
    userCollection.flatMap(_.update(selector, modifier, multi = true))
  }

  def removeUsers(selector: BSONDocument): Future[WriteResult] = {
    userCollection.flatMap(_.delete().one(selector))
  }

  def upsertUser(user: User): Future[Option[User]] = {
    userCollection.flatMap(
      _.findAndUpdate(selector = BSONDocument(User.IDDB -> user.userID),
                      update = user,
                      upsert = true,
                      fetchNewObject = true).map(_.result[User])
    )
  }

}
