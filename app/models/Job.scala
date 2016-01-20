package models

/**
  * Created by lukas on 1/20/16.
  */
class Job(val toolname: String, var state: JobState)



object Job {

  def instance(toolname: String, state: JobState) = new Job(toolname, state)
}
