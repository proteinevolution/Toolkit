package models.distributed

import models.jobs.JobState

/**
  * Created by lzimmermann on 28.04.16.
  */
object ToolkitClusterEvent {

  case class JobStateChanged(jobID : Int, sessionID: String, newState : JobState, toolname : String)
  case object  FooBar
}

