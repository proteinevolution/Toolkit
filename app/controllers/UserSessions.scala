package controllers

import models.database.users.{SessionData, User}
import modules.{CommonModule, LocationProvider}
import modules.common.HTTPRequest
import org.joda.time.DateTime
import play.api.cache._
import play.api.{Logger, mvc}
import play.api.mvc.RequestHeader
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by astephens on 24.08.16.
  */
trait UserSessions extends CommonModule {
  private val SID = "sid"
  private val USERNAME = "username"

  implicit val userCache : CacheApi
  implicit val locationProvider: LocationProvider

  /**
    *
    * Associates a user with the provided sessionID
    *
    */
  def putUser(implicit request : RequestHeader, sessionID : BSONObjectID) = {
    val httpRequest    = HTTPRequest(request)
    val newSessionData = SessionData(ip        = request.remoteAddress,
                                     userAgent = httpRequest.userAgent.getOrElse("Not Specified"),
                                     location  = locationProvider.getLocation(request))


    findUser(BSONDocument(User.SESSIONID -> sessionID)).flatMap {
      case Some(user)   =>
        Logger.info("User found by SessionID")
        val selector = BSONDocument(User.IDDB          -> user.userID)
        val modifier = BSONDocument("$set"             ->
                       BSONDocument(User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)),
                                    "$addToSet"        ->
                       BSONDocument(User.SESSIONDATA   -> newSessionData))
        modifyUser(selector,modifier).map {
          case Some(updatedUser) =>
            userCache.set(updatedUser.sessionID.get.stringify, updatedUser, 10.minutes)
            updatedUser
          case None =>
            user
        }
      case None =>
        Logger.info("User is new")
        val user = User(userID        = BSONObjectID.generate(),
                        sessionID     = Some(sessionID),
                        connected     = true,
                        sessionData   = List(newSessionData),
                        dateCreated   = Some(new DateTime()),
                        dateLastLogin = Some(new DateTime()),
                        dateUpdated   = Some(new DateTime()))
        addUser(user).map { _ =>
          user
        }
    }
  }


  /**
    * Returns a Future User
    */
  def getUser(implicit request : RequestHeader) : Future[User] = {
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

  def getUser(sessionID : BSONObjectID) : Future[Option[User]] = {
    userCache.get(sessionID.stringify) match {
      case Some(user) =>
        Future.successful(Some(user))
      case None =>
        findUser(BSONDocument(User.SESSIONID -> sessionID)).flatMap {
          case Some(user)   =>
            Logger.info("User found by SessionID")
            val selector = BSONDocument(User.IDDB          -> user.userID)
            val modifier = BSONDocument("$set"             ->
                           BSONDocument(User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)))
            modifyUser(selector,modifier).map {
              case Some(updatedUser) =>
                userCache.set(updatedUser.sessionID.get.stringify, updatedUser, 10.minutes)
                Some(updatedUser)
              case None =>
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
  def updateUserCache(user : User) = {
    //Logger.info("User WatchList is now: " + user.jobs.mkString(", "))
    userCache.set(user.sessionID.get.stringify, user, 10.minutes)
  }

  /**
    * removes a user from the sessions
    */
  def removeUser(user : User) = {
    Logger.info("Removing User: \n" + user.toString)
    userCache.remove(user.sessionID.get.stringify)
    userCollection.flatMap(_.update(BSONDocument(User.IDDB -> user.userID),
                                    BSONDocument("$set"   -> BSONDocument(User.CONNECTED -> false),
                                                 "$unset" -> BSONDocument(User.SESSIONID -> ""))))
  }

  /**
    * Handles cookie creation
    */
  def sessionCookie(implicit request : RequestHeader, sessionID : BSONObjectID, userName: Option[String]): mvc.Session = {
    request.session + (SID -> sessionID.stringify) + (USERNAME -> userName.getOrElse(""))
  }
}
