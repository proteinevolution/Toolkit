package models.sessions

import models.database.User
import org.joda.time.DateTime
import play.api.{mvc, Logger}
import play.api.mvc.RequestHeader
import reactivemongo.bson.BSONObjectID

/**
  * Created by astephens on 01.03.16.
  * Session object used for a simple creation of a session cookie with the sessionID
  */
object Session {
  val SID = "sid" // name for the entry in the session cookie
  val sessionUserMap = new scala.collection.mutable.HashMap[BSONObjectID, User]

  /**
    * Saves a User to the session Mapping for a later time
    *
    * @param sessionID The sessionID the user should be identified with.
    * @param user The user the sessionID will be linked to.wasdwasd@wasd.wasd
    */
  def addUser (sessionID : BSONObjectID, user : User) : User = {
    sessionUserMap.getOrElseUpdate(sessionID, user)
  }

  /**
    * Saves a User to the session Mapping for a later time
    *
    * @param sessionID The sessionID the user should be identified with.
    * @param user The user the sessionID will be linked to.
    */
  def editUser (sessionID : BSONObjectID, user : User) : Option[User] = {
    sessionUserMap.put(sessionID, user)
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
    sessionUserMap.getOrElse(sessionID, addUser(sessionID, User(userID = BSONObjectID.generate(),
                                                                         None,
                                                                         -1,
                                                                         None,
                                                                         Nil,
                                                                         Some(new DateTime()),
                                                                         Some(new DateTime()),
                                                                         Some(new DateTime()))))
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
    sessionUserMap.remove(sessionID)
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
    val sid = request.session.get(SID)
    val sessionID = if (sid.isDefined) {
      BSONObjectID.parse(sid.get).getOrElse(BSONObjectID.generate())
    } else {
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