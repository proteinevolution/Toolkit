package de.proteinevolution.auth

import java.time.ZonedDateTime

import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.common.LocationProvider
import de.proteinevolution.models.database.users.{ SessionData, User }
import javax.inject.{ Inject, Singleton }
import play.api.cache._
import play.api.mvc.RequestHeader
import play.api.{ mvc, Logger }
import play.mvc.Http
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONObjectID }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.hashing.MurmurHash3
@Singleton
class UserSessions @Inject()(
    userDao: UserDao,
    @NamedCache("userCache") userCache: SyncCacheApi,
    locationProvider: LocationProvider
)(implicit ec: ExecutionContext) {
  private val SID    = "sid"
  private val logger = Logger(this.getClass)

  /**
   * Creates a update modifier for the user according to the
   * @param user
   * @param sessionDataOption
   * @return
   */
  def getUserModifier(
      user: User,
      sessionDataOption: Option[SessionData] = None,
      forceSessionID: Boolean = false
  ): BSONDocument = {
    // Build the modifier - first the last login date
    BSONDocument("$set" -> BSONDocument(User.DATELASTLOGIN -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)))
      .merge(
        // In the case that the user has been emailed about their inactivity, reset that status to a regular user status
        if (user.accountType == User.CLOSETODELETIONUSER) {
          BSONDocument(
            "$set"   -> BSONDocument(User.ACCOUNTTYPE   -> 1),
            "$unset" -> BSONDocument(User.DATEDELETEDON -> "")
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
              BSONDocument("$addToSet" -> BSONDocument(User.SESSIONDATA -> sessionData))
          )
          .getOrElse(BSONDocument.empty)
      )
      .merge(
        // Add the session ID to the user
        if (forceSessionID) {
          BSONDocument(
            "$set" ->
            BSONDocument(User.SESSIONID -> Some(user.sessionID.getOrElse(BSONObjectID.generate())))
          )
        } else {
          BSONDocument.empty
        }
      )
  }

  /**
   *
   * Associates a user with the provided sessionID
   *
   */
  def putUser(implicit request: RequestHeader, sessionID: BSONObjectID): Future[User] = {
    val newSessionData = SessionData(
      ip = MurmurHash3.stringHash(request.remoteAddress).toString,
      userAgent = request.headers.get(Http.HeaderNames.USER_AGENT).getOrElse("Not specified"),
      location = locationProvider.getLocation(request)
    )

    userDao.findUser(BSONDocument(User.SESSIONID -> sessionID)).flatMap {
      case Some(user) =>
        logger.info(s"User found by SessionID:\n${user.toString}")
        val selector = BSONDocument(User.IDDB -> user.userID)

        // This resets the user's deletion date in case they have been eMailed for inactivity already
        val modifier = getUserModifier(user, Some(newSessionData))

        // Add the user to the cache and update the collection
        modifyUserWithCache(selector, modifier).map {
          case Some(updatedUser) =>
            updatedUser
          case None =>
            user
        }
      case None =>
        // Create a new user as there is no user with this sessionID
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
  }

  /**
   * Returns a Future User
   */
  def getUser(implicit request: RequestHeader): Future[User] = {
    // Ignore our monitoring service and don't update it in the DB
    if (request.remoteAddress.contentEquals("10.3.7.70")) { // TODO Put this in the config?
      Future.successful(User())
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
        case Some(user) => Future.successful(user)
        case None =>
          putUser(request, sessionID)
      }
    }
  }

  /**
   * Grabs the user with the matching sessionID from the cache, or if there is
   * none, it will try to find it in the database and put it in the cache.
   * @param sessionID
   * @return
   */
  def getUser(sessionID: BSONObjectID): Future[Option[User]] = {
    // Try the cache
    userCache.get[User](sessionID.stringify) match {
      case Some(user) =>
        // User successfully pulled from the cache
        Future.successful(Some(user))
      case None =>
        // Pull it from the DB, as it is not in the cache
        userDao.findUser(BSONDocument(User.SESSIONID -> sessionID)).flatMap {
          case Some(user) =>
            // Update the last login time
            val selector = BSONDocument(User.IDDB -> user.userID)
            val modifier = getUserModifier(user)
            modifyUserWithCache(selector, modifier).map {
              case Some(updatedUser) =>
                Some(updatedUser)
              case None =>
                // update was not possible, return the original user
                Some(user)
            }
          case None =>
            Future.successful(None)
        }
    }
  }

  /**
   * updates a user in the cache
   */
  def updateUserCache(user: User): Unit = {
    //logger.info("User WatchList is now: " + user.jobs.mkString(", "))
    user.sessionID match {
      case Some(sessionID) =>
        userCache.set(sessionID.stringify, user, 10.minutes)
      case None =>
    }
  }

  /**
   * saves the user directly to the cache after modification in the DB
   * @param selector
   * @param modifier
   * @return
   */
  def modifyUserWithCache(selector: BSONDocument, modifier: BSONDocument): Future[Option[User]] = {
    userDao
      .modifyUser(selector, modifier)
      .map(_.map { user =>
        updateUserCache(user)
        user
      })
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
        _.update(
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
