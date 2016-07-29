package models.sessions

import models.database.User
import models.misc.RandomString
import play.api.{mvc, Logger}
import play.api.mvc.RequestHeader

/**
  * Created by astephens on 01.03.16.
  * Session object used for a simple creation of a session cookie with the session_id
  */
object Session {
  val SID = "sid" // name for the entry in the session cookie
  val MID = "mid" // mainID of the last job that was get // TODO Should not be here
  var sessionUserMap  = Map.empty[String, User]

  /**
    *  Establishes a new associated of a session ID with a User
    *
    * @param sessionID The sessionID the user should be identified with.
    * @param user The user the sessionID will be linked to.
    */
  def addUser(sessionID : String, user : User) = this.sessionUserMap = sessionUserMap.updated(sessionID, user)


  /**
    * Returns a User by its sessionID, or None if the sessionID is yet not
    * associated to a User.
    *
    * @param sessionID The sessionID whose associated User should be returned
    * @return The User of the SessionID, wrapped into an Option value, or None if the sessionID is not associated
    *         with a User.
    */
  def getUser(sessionID : String) : Option[User] = sessionUserMap.get(sessionID)


  /**
    * Removes a user by its sessionID and returns the removed User as Option[User],
    * or None if the User was not present.
    *
    * @param sessionID The sessionID of the user which should be removed.
    * @return Option[User] of the User which was removed, None if no user was removed because of a
    *         non-present sessionID
    */
  def removeUser (sessionID : String) : Option[User] = {
    sessionUserMap.get(sessionID).map { user =>
      this.sessionUserMap = this.sessionUserMap - sessionID ; user  }
  }



  /**
    * Generates a new Session ID unless the user already has one.
    * The Session ID is saved in the sessions array
    *
    * @param request
    * @return
    */
  def requestSessionID(request : RequestHeader) : String = {
    request.session.get(SID).getOrElse {
      newSessionID(request)
    }
  }

  /**
    * Generates a new Session ID.
    * The Session ID is saved in the sessions array
    *
    * @param request
    * @return
    */
  def newSessionID(request : RequestHeader) : String = {
    var nextString  = ""
    do {
      nextString = RandomString.randomAlphaNumString(15)
    } while (sessionUserMap.contains(nextString))
    nextString
  }

  /**
    * Generates the Cookie for the Session
    *
    * @param request
    * @param session_id
    * @return
    */
  def closeSessionRequest(request : RequestHeader, session_id : String): mvc.Session = {
    Logger.info("Request from SID \"" + session_id + "\"")
    request.session + (SID -> session_id)
  }

  /**
    * Generates the Cookie for the Session
    *
    * @param request
    * @param session_id
    * @param mainID
    * @return
    */
  def closeSessionRequest(request : RequestHeader, session_id : String, mainID : Long): mvc.Session = {
    Logger.info("Request from SID \"" + session_id + "\"")
    request.session + (SID -> session_id)
    request.session + (MID -> mainID.toString)
  }
}