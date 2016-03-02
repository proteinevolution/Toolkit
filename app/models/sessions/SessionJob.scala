package models.sessions

/**
  * Created by astephens on 02.03.16.
  */
class SessionJob(val session_id : String,   // The session ID of the User
                 val job_id     : String) { // The job ID
}

object SessionJob {
  def apply (session_id : String, job_id : String) : SessionJob = {
    new SessionJob(session_id, job_id)
  }
}

//SessionJob Class used for database storage
case class DBSessionJob(val session_id : String, val job_id : String)