package models.database

import org.joda.time.DateTime
import play.api.mvc.RequestHeader
import play.api.{Logger, mvc}
import reactivemongo.bson._
import modules.common.HTTPRequest

/**
  * Created by astephens on 01.03.16.
  * Session object used for a simple creation of a session cookie with the sessionID
  */

case class SessionData(sessionID   : BSONObjectID,                    // Session ID of the User
                       userID      : BSONObjectID,                    // User ID will be stored
                       dateCreated : Option[DateTime],                // Creation time of the Session
                       dateUpdated : Option[DateTime])                // Last Visit

object Sessions {
  val sessionUserMap = new scala.collection.mutable.HashMap[BSONObjectID, User]
}

trait Session extends {
  // ID entry in the session cookie
  val SID            = "sid"

  /**
    * Saves a User to the session Mapping for a later time
    *
    * @param sessionID The sessionID the user should be identified with.
    * @param user The user the sessionID will be linked to.wasdwasd@wasd.wasd
    */
  def addUser (sessionID : BSONObjectID, user : User) : User = {
    Sessions.sessionUserMap.getOrElseUpdate(sessionID, user)
  }

  /**
    * Saves a User to the session Mapping for a later time
    *
    * @param sessionID The sessionID the user should be identified with.
    * @param user The user the sessionID will be linked to.
    */
  def editUser (sessionID : BSONObjectID, user : User) : Option[User] = {
    Sessions.sessionUserMap.put(sessionID, user)
  }

  /**
    * Returns a User by its sessionID, or None if the sessionID is yet not
    * associated to a User.
    *
    * @param sessionID The sessionID whose associated User should be returned
    * @return The User of the SessionID, wrapped into an Option value, or None if the sessionID is not associated
    *         with a User.
    */
  def getUser (sessionID : BSONObjectID) : User = {
    Sessions.sessionUserMap.getOrElse(sessionID,
                                        addUser(sessionID, User(userID        = BSONObjectID.generate(),
                                                                sessionID     = Some(sessionID),
                                                                sessionData   = BSONDocument(),
                                                                accountType   = -1,
                                                                jobs          = Nil,
                                                                dateCreated   = Some(new DateTime()),
                                                                dateLastLogin = Some(new DateTime()),
                                                                dateUpdated   = Some(new DateTime()))))
  }




  /**
    * Returns a User by its sessionID, or None if the sessionID is yet not
    * associated to a User.
    *
    * @param request The request which can identify the user
    * @return The User of the SessionID, wrapped into an Option value, or None if the sessionID is not associated
    *         with a User.
    */
  def getUser (implicit request: RequestHeader) : User = {
    getUser(requestSessionID)
  }

  /**
    * Removes a user by its sessionID and returns the removed User as Option[User],
    * or None if the User was not in the mapping.
    *
    * @param sessionID The sessionID of the user which should be removed.
    * @return Option[User] of the User which was removed, None if no user was removed because of a
    *         non-present sessionID
    */
  def removeUser (sessionID : BSONObjectID) : Option[User] = {
    Sessions.sessionUserMap.remove(sessionID)
  }

  /**
    * Removes a user by its sessionID and returns the removed User as Option[User],
    * or None if the User was not in the mapping.
    *
    * @param request The HTTP request
    * @return Option[User] of the User which was removed, None if no user was removed because of a
    *         non-present sessionID
    */
  def removeUser (implicit request: RequestHeader) : Option[User] = {
    removeUser(requestSessionID)
  }

  /**
    * Generates a new Session ID unless the user already has one.
    * The Session ID is saved in the sessions array
    *
    * @param request
    * @return
    */
  def requestSessionID(implicit request: RequestHeader) : BSONObjectID = {
    val sessionID = request.session.get(SID) match {
      case Some(sid) =>
      BSONObjectID.parse(sid).getOrElse(BSONObjectID.generate())
      case None      =>
      BSONObjectID.generate()
    }

    Logger.info("Request from SID \"" + sessionID.stringify + "\"")
    sessionID
  }

  /**
    * Generates a new Session ID.
    * The Session ID is saved in the sessions array
    *
    * @param request
    * @return
    */
  def newSessionID(implicit request : RequestHeader) : BSONObjectID = {
    BSONObjectID.generate()
  }

  /**
    * Generates the Cookie for the Session
    *
    * @param request
    * @param sessionID
    * @return
    */
  def closeSessionRequest(implicit request : RequestHeader, sessionID : BSONObjectID): mvc.Session = {
    request.session + (SID -> sessionID.stringify)
  }
}