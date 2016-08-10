package models.sessions

import models.database.User
import play.api.{mvc, Logger}
import play.api.mvc.RequestHeader
import reactivemongo.bson.BSONObjectID

import scala.collection.mutable.ArrayBuffer

/**
  * Created by astephens on 01.03.16.
  * Session object used for a simple creation of a session cookie with the sessionID
  */
object Session {
  val SID = "sid" // name for the entry in the session cookie
  val sessions : ArrayBuffer[String] = ArrayBuffer.empty
  val sessionUserMap  = new scala.collection.mutable.HashMap[String, User]

  /**
    * Saves a User to the session Mapping for a later time
    *
    * @param sessionID The sessionID the user should be identified with.
    * @param user The user the sessionID will be linked to.wasdwasd@wasd.wasd
    */
  def addUser (sessionID : BSONObjectID, user : User) : User = {
    sessionUserMap.getOrElseUpdate(sessionID.stringify, user)
  }

  /**
    * Saves a User to the session Mapping for a later time
    *
    * @param sessionID The sessionID the user should be identified with.
    * @param user The user the sessionID will be linked to.
    */
  def editUser (sessionID : BSONObjectID, user : User) : Option[User] = {
    sessionUserMap.put(sessionID.stringify, user)
  }

  /**
    * Returns a User by its sessionID, or None if the sessionID is yet not
    * associated to a User.
    *
    * @param sessionID The sessionID whose associated User should be returned
    * @return The User of the SessionID, wrapped into an Option value, or None if the sessionID is not associated
    *         with a User.
    */
  def getUser (sessionID : BSONObjectID) : Option[User] = {
    sessionUserMap.get(sessionID.stringify)
  }

  /**
    * Returns a User by its sessionID, or None if the sessionID is yet not
    * associated to a User.
    *
    * @param request The request which can identify the user
    * @return The User of the SessionID, wrapped into an Option value, or None if the sessionID is not associated
    *         with a User.
    */
  def getUser (implicit request: RequestHeader) : Option[User] = {
    sessionUserMap.get(requestSessionID.stringify)
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
    sessionUserMap.remove(sessionID.stringify)
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
    if (sid.isDefined) {
      BSONObjectID.parse(sid.get).getOrElse(BSONObjectID.generate())
    } else {
      BSONObjectID.generate()
    }
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
    Logger.info("Request from SID \"" + sessionID.stringify + "\"")
    request.session + (SID -> sessionID.stringify)
  }
}