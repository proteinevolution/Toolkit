package models.distributed

import models.jobs.JobState

/**
  * Created by lzimmermann on 28.04.16.
  */
case class JobStateChanged(jobID : Int, newState : JobState, toolname : String)
case object  FooBar

