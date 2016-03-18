package models.jobs

import actors.Link
import actors.UserActor.{Convert, UpdateJob}
import akka.actor.ActorRef
import models.graph.{Locked, File, FileState}
import play.api.Logger

import scala.collection.mutable.ArrayBuffer

/**
  * Created by lukas on 1/20/16.
  */
class UserJob(val userActor      : ActorRef, // Which UserActor the Job belongs to
              val toolname       : String,   // The name of the associated tool
              val job_id         : String,   // Which job_id is attached to this Job
              val user_id        : Long,     // Which user_id is attached to this job
              private var state  : JobState, // State of the job
              var startImmediate : Boolean)
{
  // TODO Toolname is a redundant field in the UserJob
  // TODO Pass Main ID instead of user and job ID to make sure a unique job ID is used.

  val tool = models.graph.Ports.nodeMap(toolname)  // The associated tool node

  // Keeps track of all child Jobs and which inport links of the child jobs are controlled
  val childJobs : ArrayBuffer[(UserJob, Seq[Link])] = ArrayBuffer.empty

  // The process that is associated with the Execution of this job.
  var process : Option[scala.sys.process.Process] = None

  var destroyed : Boolean = false

  // Maps all input files to an associated file object
   val inFileStates = tool.inports.map { port =>

    port.filename -> File.in(port.filename, this)
  }.toMap



  def appendChild(userJob : UserJob, links : Seq[Link]): Unit = {

    if(!destroyed) {
      childJobs.append((userJob, links))

      // Lock all files in the inport port of the child job
      links.foreach { link =>

        // lock each inlink file from child Job
        val filename = userJob.tool.inports(link.in).filename
        userJob.changeInFileState(filename, Locked)
      }
      // if the Job is done, we can trigger conversion process for this Job
      if (state == Done) {
        userActor ! Convert(job_id, userJob.job_id, links)
      }
    }
  }


  def changeState(newState : JobState): Unit = {

    if(!destroyed) {
      state = newState
      userActor ! UpdateJob(this)

      // If the Job state is Done, we ask the UserActor to convert Output for all child jobs
      if(newState == Done) {

        childJobs.foreach {userJob =>

            userActor ! Convert(job_id, userJob._1.job_id, userJob._2)
        }
      }
    }
  }

  def destroy() = {

      // TODO We have to handle the case that this Job has child jobs
      if(process.isDefined) {

        process.get.destroy()
      }
      process = None

    destroyed = true
  }




  // Counts the number of files that have notified to be ready. If all files are ready, then
  // we can set the JobState to be prepared
  private var readyCounter = 0

  def countReady() = {

    readyCounter += 1
    Logger.info("Ready counter is now: " + readyCounter)

    readyCounter match {

      // If all files are Ready, we can set the job to be *Prepared*
      case tool.noInfiles => changeState(Prepared)

      // Otherwise, we have seen at least one file to be ready, so the job is *Partially Prepared*
      case  _  : Int => changeState(PartiallyPrepared)
    }
  }


  def changeInFileState(filename : String, state : FileState) = {
    if(inFileStates.contains(filename)) inFileStates(filename).changeState(state)
  }


  def getState = state
}

object UserJob {


  def apply(userActor      : ActorRef,
            tool           : String,
            job_id         : String,
            user_id        : Long,
            jobState       : JobState,
            startImmediate : Boolean) = {
    new UserJob(userActor, tool, job_id, user_id, jobState, startImmediate)
  }
}


