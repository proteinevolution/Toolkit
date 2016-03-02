package models.sessions
import models.misc.RandomString
import play.api.{mvc, Logger}
import play.api.mvc.{AnyContent, Request}

/**
  * Created by astephens on 01.03.16.
  * Class represents a Session saved in the DB
  */
class Session(val session_id : String,   // The session ID of the User
              val user_id    : Long,     // The user ID
              val session_ip : String) { // The IP of the User
}

// Session object used for the creation of new session objects
object Session {
  val SID = "sid" // name for the entry in the session cookie

  var sessions = List[String]()
  var counter = 0

  def requestSessionID(request : Request[AnyContent]) : String = {
    request.session.get(SID).getOrElse {
      counter += 1
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

//Session Class used for database storage
case class DBSession(val session_id : String, val user_id : Long, val session_ip : String)