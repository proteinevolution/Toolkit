package models.jobs

import actors.Link
import actors.UserActor.{Convert, JobStateChanged}
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
              val startImmediate : Boolean)
{
  // TODO Toolname is a redundant field in the UserJob
  userActor ! JobStateChanged(job_id, state)

  val tool = models.graph.Ports.nodeMap(toolname)  // The associated tool node

  // Keeps track of all child Jobs and which inport links of the child jobs are controlled
  val childJobs : ArrayBuffer[(UserJob, Seq[Link])] = ArrayBuffer.empty


  // Maps all input files to an associated file object
   val inFileStates = tool.inports.map { port =>

    port.filename -> File.in(port.filename, this)
  }.toMap



  def appendChild(userJob : UserJob, links : Seq[Link]): Unit = {

    Logger.info("???????????????????????????????????????????????????????????????????")
    childJobs.append((userJob, links))
    // Lock all files in the inport port of the child job
    links.foreach { link =>

      // lock each inlink file from child Job
      val filename =  userJob.tool.inports(link.in).filename
      userJob.changeInFileState(filename, Locked)

    }
    // if the Job is done, we can trigger conversion process for this Job
    if(state == Done) {

      Logger.info("Send convert to User Actor")
      userActor ! Convert(job_id, userJob.job_id, links)
    }
  }


  def changeState(newState : JobState): Unit = {

    userActor ! JobStateChanged(job_id, newState)

    // If the new state is Done, we can make all output files ready
    if(newState == Done) {

      Logger.info("Job Done, we need to convert for " + childJobs.length + "sjobs")

      childJobs.foreach {userJob =>

          userActor ! Convert(job_id, userJob._1.job_id, userJob._2)
      }
    }
    state = newState
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

    inFileStates(filename).changeState(state)
    /*
    [error] a.a.OneForOneStrategy - key not found: format
java.util.NoSuchElementException: key not found: format
	at scala.collection.MapLike$class.default(MapLike.scala:228)
	at scala.collection.AbstractMap.default(Map.scala:59)
	at scala.collection.MapLike$class.apply(MapLike.scala:141)
	at scala.collection.AbstractMap.apply(Map.scala:59)
  at models.jobs.UserJob.changeInFileState(UserJob.scala:100)	TODO Error
	at actors.Worker$$anonfun$receive$1$$anonfun$applyOrElse$8.apply(Worker.scala:121)
	at actors.Worker$$anonfun$receive$1$$anonfun$applyOrElse$8.apply(Worker.scala:117)
	at scala.collection.TraversableLike$WithFilter$$anonfun$foreach$1.apply(TraversableLike.scala:778)
	at scala.collection.immutable.Map$Map3.foreach(Map.scala:161)
	at scala.collection.TraversableLike$WithFilter.foreach(TraversableLike.scala:777)
     */
  }


  def getState = state
}

object UserJob {

  def apply(userActor      : ActorRef,
            tool           : String,
            job_id         : String,
            user_id        : Long,
            startImmediate : Boolean) = {
    new UserJob(userActor, tool, job_id, user_id, Submitted, startImmediate)
  }

  def apply(userActor      : ActorRef,
            tool           : String,
            job_id         : String,
            user_id        : Long,
            jobState       : JobState,
            startImmediate : Boolean) = {
    new UserJob(userActor, tool, job_id, user_id, jobState, startImmediate)
  }
}


