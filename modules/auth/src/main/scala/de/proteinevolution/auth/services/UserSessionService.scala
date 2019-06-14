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

package de.proteinevolution.auth.services

import java.time.ZonedDateTime

import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.base.helpers.ToolkitTypes
import de.proteinevolution.user.{ SessionData, User }
import de.proteinevolution.util.LocationProvider
import javax.inject.{ Inject, Singleton }
import play.api.cache._
import play.api.mvc.RequestHeader
import play.api.{ mvc, Logging }
import play.mvc.Http
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONObjectID }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.hashing.MurmurHash3

@Singleton
class UserSessionService @Inject()(
    userDao: UserDao,
    @NamedCache("userCache") userCache: SyncCacheApi,
    locationProvider: LocationProvider
)(implicit ec: ExecutionContext)
    extends ToolkitTypes
    with Logging {

  private val SID = "sid"

  def getUser(implicit request: RequestHeader): Future[User] = {
    // Ignore our monitoring service and don't update it in the DB
    if (request.remoteAddress.contentEquals("10.3.7.70")) { // TODO Put this in the config?
      fuccess(User())
    } else {
      val sessionID = request.session.get(SID) match {
        case Some(sid) =>
          // Check if the session ID is parseable - otherwise generate a new one
          BSONObjectID.parse(sid).getOrElse(BSONObjectID.generate())
        case None =>
          BSONObjectID.generate()
      }
      // cache related stuff should remain in the project where the cache is bound
      userCache.get[User](sessionID.stringify) match {
        case Some(user) => fuccess(user)
        case None => // session not known yet in cache
          val newSessionData = SessionData(
            ip = MurmurHash3.stringHash(request.remoteAddress).toString,
            userAgent = request.headers.get(Http.HeaderNames.USER_AGENT).getOrElse("Not specified"),
            location = locationProvider.getLocation(request)
          )

          userDao.findUserBySessionId(sessionID).flatMap {
            case Some(user) =>
              logger.info(s"User found by SessionID:\n${user.toString}")
              // Add the user to the cache and update the collection
              userDao.saveNewLogin(user, Some(newSessionData)).map {
                case Some(updatedUser) =>
                  updateUserInCache(updatedUser)
                case None =>
                  user
              }
            case None =>
              saveNewAnonymousUser(sessionID, newSessionData)
          }
      }
    }
  }

  def saveNewAnonymousUser(sessionID: BSONObjectID, newSessionData: SessionData)(
      implicit request: RequestHeader
  ): Future[User] = {
    // Create a new anonymous user as there is no user with this sessionID
    val user = User(
      userID = BSONObjectID.generate(),
      sessionID = Some(sessionID),
      sessionData = List(newSessionData),
      dateCreated = Some(ZonedDateTime.now),
      dateLastLogin = Some(ZonedDateTime.now),
      dateUpdated = Some(ZonedDateTime.now)
    )
    userDao.addUser(user).map { _ =>
      logger.info(s"User is new:\n${user.toString}\nIP: ${request.remoteAddress.toString}")
      user
    }
  }

  /**
   * Grabs the user with the matching sessionID from the cache, or if there is
   * none, it will try to find it in the database and put it in the cache.
   * Only used for the websocket where no request object is available.
   *
   * @param sessionID
   * @return
   */
  def getUserBySessionID(sessionID: BSONObjectID): Future[Option[User]] = {
    // Try the cache
    userCache.get[User](sessionID.stringify) match {
      case Some(user) =>
        // User successfully pulled from the cache
        fuccess(Some(user))
      case None =>
        // Pull it from the DB, as it is not in the cache
        userDao.findUserBySessionId(sessionID).flatMap {
          case Some(user) =>
            // Update the last login time
            userDao.saveNewLogin(user).map {
              case Some(updatedUser) =>
                Some(updateUserInCache(updatedUser))
              case None =>
                // update was not possible, return the original user
                Some(user)
            }
          case None =>
            fuccess(None)
        }
    }
  }

  /**
   * updates a user in the cache
   */
  def updateUserInCache(user: User): User = {
    //logger.info("User WatchList is now: " + user.jobs.mkString(", "))
    user.sessionID.foreach { sessionID =>
      userCache.set(sessionID.stringify, user, 10.minutes)
    }
    user
  }

  /**
   * removes a user from the sessions and the database
   */
  def removeUserFromCache(user: User, withDB: Boolean = true): Any = {
    logger.info("Removing User: \n" + user.toString)
    // Remove user from the cache
    user.sessionID.foreach(sessionID => userCache.remove(sessionID.stringify))

    if (withDB) {
      userDao.userCollection.flatMap(
        _.update(ordered = false).one(
          BSONDocument(User.IDDB -> user.userID),
          BSONDocument(
            "$set" ->
            BSONDocument(
              User.DATELASTLOGIN -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
            ),
            "$unset" ->
            BSONDocument(
              User.SESSIONID -> "",
              User.CONNECTED -> ""
            )
          )
        )
      )
    }
  }

  /**
   * Handles cookie creation
   */
  def sessionCookie(implicit request: RequestHeader, sessionID: BSONObjectID): mvc.Session = {
    request.session + (SID -> sessionID.stringify)
  }
}
