package controllers

import com.typesafe.config.ConfigFactory
import models.database.{User, SessionData}
import modules.GeoIP
import modules.common.HTTPRequest
import org.joda.time.DateTime
import play.api.cache.CacheApi
import play.api.mvc
import play.api.mvc.RequestHeader
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONObjectID}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by astephens on 24.08.16.
  */
trait UserSessions extends GeoIP {
  private final val SID = "sid"

  /**
    * puts a user in the cache
    */
  def putUser(implicit request : RequestHeader, sessionID : BSONObjectID, userCollection : Future[BSONCollection], userCache : CacheApi) = {
    val httpRequest    = HTTPRequest(request)
    val newSessionData = SessionData(ip        = request.remoteAddress,
                                     userAgent = httpRequest.userAgent.getOrElse("Not Specified"),
                                     location  = getLocation(request))


    userCollection.flatMap(_.find(BSONDocument(User.SESSIONID -> sessionID)).one[User]).map {
      case Some(user)   =>
        val selector = BSONDocument(User.IDDB          -> user.userID)
        val modifier = BSONDocument("$set"             ->
          BSONDocument(User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)),
          "$addToSet"        ->
            BSONDocument(User.SESSIONDATA   -> newSessionData))
        userCollection.flatMap(_.update(selector,modifier))
        user.dateLastLogin.flatMap(dateLastLogin => Some(new DateTime()))
        userCache.set(user.sessionID.get.stringify, user, 10.minutes)
        user
      case None =>
        val user = User(userID        = BSONObjectID.generate(),
                        sessionID     = Some(sessionID),
                        up            = Some(true),
                        sessionData   = List(newSessionData),
                        dateCreated   = Some(new DateTime()),
                        dateLastLogin = Some(new DateTime()),
                        dateUpdated   = Some(new DateTime()))
        userCollection.flatMap(_.insert(user))
        userCache.set(user.sessionID.get.stringify, user, 10.minutes)
        user
    }
  }


  /**
    * Returns a Future User
    */
  def getUser(implicit request : RequestHeader, userCollection : Future[BSONCollection], userCache : CacheApi) : Future[User] = {
    val sessionID = request.session.get(SID) match {
      case Some(sid) =>
        BSONObjectID.parse(sid).getOrElse(BSONObjectID.generate())
      case None =>
        BSONObjectID.generate()
    }
    userCache.get[User](sessionID.stringify) match {
      case Some(user) =>
        Future.successful(user)
      case None       =>
        putUser(request, sessionID, userCollection, userCache)
    }
  }

  /**
    * updates a user in the cache
    */
  def updateUser(user : User, userCache : CacheApi) = {
    userCache.set(user.sessionID.get.stringify, user)
  }

  /**
    * Handles cookie creation
    */
  def sessionCookie(implicit request : RequestHeader, sessionID : BSONObjectID): mvc.Session = {
    request.session + (SID -> sessionID.stringify)
  }
}
