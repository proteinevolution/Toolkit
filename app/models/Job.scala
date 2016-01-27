package models


/**
  * Created by lukas on 1/20/16.
  */
class Job(toolname_c: String, state_c: JobState, id_c : String, uid_c : String) {

  val toolname : String = toolname_c
  var state : JobState = state_c
  var id : String = id_c
  val uid : String = uid_c

  def attachParams(params : Map[String, Any]) = new SuppliedJob(toolname, state, id, uid, params)

}


// Job which is supplied with parameters and ready for execution
class SuppliedJob(toolname : String, state : JobState, id : String, uid : String, params_c : Map[String, Any])
  extends Job(toolname, state, id, uid) {

  val params : Map[String, Any] = params_c
}


object Job {

  def apply(toolname: String, state: JobState, id : String, uid : String) = new Job(toolname, state, id, uid)
}
