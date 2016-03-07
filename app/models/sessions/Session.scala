package models.sessions
import models.misc.RandomString
import play.api.{mvc, Logger}
import play.api.mvc.{AnyContent, Request}

/**
  * Created by astephens on 01.03.16.
  * Session object used for a simple creation of a session cookie with the session_id
  */
object Session {
  val SID = "sid" // name for the entry in the session cookie

  var sessions = List[String]()

  def requestSessionID(request : Request[AnyContent]) : String = {
    request.session.get(SID).getOrElse {
      var nextString:String = ""
      do {
        nextString = RandomString.randomAlphaNumString(15)
      } while (sessions.contains(nextString))

      sessions ::= nextString
      nextString
    }
  }

  def closeSessionRequest(request : Request[AnyContent], session_id : String): mvc.Session = {
    Logger.info("Request from SID \"" + session_id + "\"")
    request.session + (SID -> session_id)
  }
}