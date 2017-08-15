package models

import javax.inject.{Inject, Singleton}

import models.database.users.{SessionData, User}
import modules.LocationProvider
import modules.common.HTTPRequest
import modules.db.MongoStore
import org.joda.time.DateTime
import play.api.cache._
import play.api.mvc.RequestHeader
import play.api.{Logger, mvc}
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.hashing.MurmurHash3

/**
  * Created by astephens on 24.08.16.
  */
@Singleton
class UserSessions @Inject()(mongoStore: MongoStore,
                             @NamedCache("userCache") val userCache: CacheApi,
                             locationProvider: LocationProvider) {
  private val SID      = "sid"

  /**
    *
    * Associates a user with the provided sessionID
    *
    */
  def putUser(implicit request: RequestHeader, sessionID: BSONObjectID): Future[User] = {
    val httpRequest = HTTPRequest(request)
    val newSessionData = SessionData(ip        = MurmurHash3.stringHash(request.remoteAddress).toString,
                                     userAgent = httpRequest.userAgent.getOrElse("Not Specified"),
                                     location  = locationProvider.getLocation(request))

    mongoStore.findUser(BSONDocument(User.SESSIONID -> sessionID)).flatMap {
      case Some(user) =>
        Logger.info("User found by SessionID")
        val selector = BSONDocument(User.IDDB -> user.userID)
        val modifier = BSONDocument("$set" ->
                                    BSONDocument(User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)),
                                    "$addToSet" ->
                                    BSONDocument(User.SESSIONDATA -> newSessionData))
        modifyUserWithCache(selector, modifier).map {
          case Some(updatedUser) =>
            updatedUser
          case None =>
            user
        }
      case None =>
        Logger.info("User is new")
        val user = User(
          userID = BSONObjectID.generate(),
          sessionID = Some(sessionID),
          sessionData = List(newSessionData),
          dateCreated = Some(new DateTime()),
          dateLastLogin = Some(new DateTime()),
          dateUpdated = Some(new DateTime())
        )
        mongoStore.addUser(user).map { _ =>
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
      userCache.get(sessionID.stringify) match {
        case Some(user) =>
          Future.successful(user)
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
    userCache.get(sessionID.stringify) match {
      case Some(user) =>
        // User successfully pulled from the cache
        Future.successful(Some(user))
      case None =>
        // Pull it from the DB, as it is not in the cache
        mongoStore.findUser(BSONDocument(User.SESSIONID -> sessionID)).flatMap {
          case Some(user) =>
            // There is a user in the DB
            //Logger.info("User found in collection by sessionID")
            // Update the last login time
            val selector = BSONDocument(User.IDDB -> user.userID)
            val modifier = BSONDocument(
              "$set" ->
              BSONDocument(User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis))
            )
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
    //Logger.info("User WatchList is now: " + user.jobs.mkString(", "))
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
    mongoStore
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
    Logger.info("Removing User: \n" + user.toString)
    // Remove user from the cache
    user.sessionID.foreach(sessionID => userCache.remove(sessionID.stringify))

    if (withDB) {
      mongoStore.userCollection.flatMap(
        _.update(BSONDocument(User.IDDB -> user.userID),
                 BSONDocument("$unset"  -> BSONDocument(User.SESSIONID -> "", User.CONNECTED -> "")))
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
