package models.jobs

import actors.UserActor.JobStateChanged
import akka.actor.ActorRef

/**
  * Created by lukas on 1/20/16.
  */
class UserJob(val userActor : ActorRef, // Which UserActor the Job belongs to
              val tool_name : String, // The name of the associated tool
              private var state: JobState, // The state in which this job is currently in
              val job_id : String, // Which job_id is attached to this Job
              val user_id : Long) // Which user_id is attached to this job
{

  def changeState(newState : JobState): Unit = {

    userActor ! JobStateChanged(job_id, newState)
    state = newState
  }

  def getState = state
}

object UserJob {

  def apply(userActor : ActorRef, tool: String, state: JobState, job_id : String, user_id : Long) = {

    val newUserJob = new UserJob(userActor, tool, state, job_id, user_id)
    newUserJob.changeState(state)
    newUserJob
  }
}

//Job Class used for database storage
case class DBJob(val job_id : String, val user_id : Long, tool_name : String)
