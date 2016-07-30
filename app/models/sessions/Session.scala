package models.sessions

import models.database.MongoDBUser
import models.misc.RandomString
import play.api.{mvc, Logger}
import play.api.mvc.RequestHeader
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by astephens on 01.03.16.
  * Session object used for a simple creation of a session cookie with the session_id
  */
object Session {
  val SID = "sid" // name for the entry in the session cookie
  val MID = "mid" // mainID of the last job that was get
  val sessions : ArrayBuffer[String] = ArrayBuffer.empty
  val sessionUserMap  = new mutable.HashMap[String, MongoDBUser]

  /**
    * identifies a User
    *
    * @param session_id
    * @param user
    */
  def addUser (session_id : String, user : MongoDBUser) {
    sessionUserMap.getOrElseUpdate(session_id, user)
  }

  /**
    * Returns a User by their Session ID
    *
    * @param session_id
    * @return
    */
  def getUser (session_id : String) : Option[MongoDBUser] = {
    sessionUserMap.get(session_id)
  }

  /**
    * Removes a user from the Hashmap
    *
    * @param session_id
    * @return
    */
  def removeUser (session_id : String) : Option[MongoDBUser] = {
    sessionUserMap.remove(session_id)
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
    } while (sessions.contains(nextString))

    sessions append nextString
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