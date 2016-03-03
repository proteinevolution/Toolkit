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
              val user_id : String, // Which user_id is attached to this job
              val startImmediate : Boolean)
{

  // TODO Toolname is a redundant field in the UserJob
  private var state : JobState = Submitted
  userActor ! JobStateChanged(job_id, Submitted)

  // The associated tool node
  val tool = models.graph.Ports.nodeMap(toolname)


  private var inFileStates : Map[String, FileState] = tool.inports.flatMap { port =>

    port.files.map { f =>

      f -> Missing
    }
  }.toMap



  // Counts the number of files that have notified to be ready. If all files are ready, then
  // we can set the JobState to be prepared
  private var readyCounter = 0

  def countReady() = {

    readyCounter += 1

    state = readyCounter match {

      // If all files are Ready, we can set the job to be *Prepared*
      case tool.noInfiles => Prepared

      // Otherwise, we have seen at least one file to be ready, so the job is *Partially Prepared*
      case  _  : Int => PartiallyPrepared
    }
  }


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

  def apply(userActor : ActorRef, tool: String, job_id : String, user_id : String, startImmediate : Boolean) = {

      new UserJob(userActor, tool, job_id, user_id, startImmediate)
  }
}

//Job Class used for database storage
case class DBJob(val job_id : String, val user_id : String, toolname : String)
