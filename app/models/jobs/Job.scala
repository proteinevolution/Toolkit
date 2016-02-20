package models.jobs

/**
  * Created by lukas on 1/20/16.
  */
class Job(val toolname: String,
          var state: JobState,
          var id : String,
          val uid : String) {

  def attachParams(params : Map[String, Any]) = new SuppliedJob(toolname, state, id, uid, params)
}


// Job which is supplied with parameters and ready for execution
class SuppliedJob(toolname : String,
                  state : JobState,
                  id : String,
                  uid : String,
                  val params : Map[String, Any])
  extends Job(toolname, state, id, uid) {

  def stripParams() = new Job(toolname, state, id, uid)
}


object Job {

  def apply(toolname: String, state: JobState, id : String, uid : String) = new Job(toolname, state, id, uid)
}

//Job Class used for database storage
case class DBJob(val job_id : String, val user_id : Long)