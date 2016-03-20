package models.sessions
import models.misc.RandomString
import play.api.{mvc, Logger}
import play.api.mvc.RequestHeader
import scala.collection.mutable.ArrayBuffer

/**
  * Created by astephens on 01.03.16.
  * Session object used for a simple creation of a session cookie with the session_id
  */
object Session {

  val SID = "sid" // name for the entry in the session cookie
  val sessions : ArrayBuffer[String] = ArrayBuffer.empty

  def requestSessionID(request : RequestHeader) : String = {
    request.session.get(SID).getOrElse {
      var nextString  = ""
      do {
        nextString = RandomString.randomAlphaNumString(15)
      } while (sessions.contains(nextString))

      sessions append nextString
      nextString
    }
  }

  def closeSessionRequest(request : RequestHeader, session_id : String): mvc.Session = {
    Logger.info("Request from SID \"" + session_id + "\"")
    request.session + (SID -> session_id)
  }
}