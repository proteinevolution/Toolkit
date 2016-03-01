package models.jobs

import actors.UserActor.JobStateChanged
import akka.actor.ActorRef
import models.graph.{Missing, FileState}

/**
  * Created by lukas on 1/20/16.
  */
class UserJob(val userActor : ActorRef, // Which UserActor the Job belongs to
              val toolname : String, // The name of the associated tool
              val job_id : String, // Which job_id is attached to this Job
              val user_id : Long,
              val startImmediate : Boolean) // Which user_id is attached to this job
{

  private var state : JobState = Submitted
  userActor ! JobStateChanged(job_id, Submitted)

  val tool = models.graph.Ports.nodeMap(toolname)


  private var inFileStates : Map[String, FileState] = tool.inports.flatMap { port =>

    port.files.map { f =>

      f -> Missing
    }
  }.toMap

  private var outFileStates : Map[String, FileState] = tool.outports.flatMap { port =>

    port.files.map { f =>

      f -> Missing
    }
  }.toMap


  def changeInFileState(filename : String, state : FileState) = {

    inFileStates = inFileStates.updated(filename, state)
  }


  def changeState(newState : JobState): Unit = {

    userActor ! JobStateChanged(job_id, newState)
    state = newState
  }



  def getState = state
}

object UserJob {

  def apply(userActor : ActorRef, tool: String, job_id : String, user_id : Long, startImmediate : Boolean) = {

      new UserJob(userActor, tool, job_id, user_id, startImmediate)
  }
}

//Job Class used for database storage
case class DBJob(val job_id : String, val user_id : Long, toolname : String)
