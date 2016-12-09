package actors

import javax.inject.{Inject, Named}

import actors.JobActor._
import actors.Master.CreateJob
import akka.actor.{Actor, ActorRef, FSM}
import com.google.inject.assistedinject.Assisted
import models.Constants
import models.database._
import modules.tel.TEL
import modules.tel.runscripts._
import play.api.Logger
import better.files._
import modules.tel.runscripts.Runscript.Evaluation

import scala.concurrent.duration._


/**
  * Created by lzimmermann on 02.12.16.
  */


object JobActor {


  // The JobActor is either Idle or represent a Jobstate
  sealed trait JobActorState
  case class Employed(state: JobState) extends JobActorState
  case object Unemployed extends JobActorState


  // Data that can be send to the JobActor via a message
  sealed trait JobData
  case object Empty extends JobData
  case class RunscriptData(toolname: String, params: Map[String, String]) extends JobData

  // Data that the JobActor operates on
  sealed trait JobActorData
  case class ParameterSupply(params: Map[String, Boolean], toolname: String) extends JobActorData

  trait Factory {

    def apply : Actor
  }

}

class JobActor @Inject() (runscriptManager : RunscriptManager,
                          @Named("master") master: ActorRef)
  extends Actor with FSM[JobActorState, JobData] with Constants {

  var currentJobID : Option[String] = None
  var executionContext: Option[ExecutionContext] = None


  /** Supplies a value for a particular Parameter. Returns params again if the parameter
    * is not present
    * @param name
    * @param value
    * @param params
    */
  private def supply(name: String, value: String, params: Seq[(String, (Runscript.Evaluation, Option[Argument]))])
  : Seq[(String, (Runscript.Evaluation, Option[Argument]))] = {

      params.map  {
        case (paramName, (evaluation, _)) if paramName == name =>

          val x = Some(evaluation(RString(value), executionContext.get))
          (name, (evaluation, x))

        case q => q
      }
  }

  /**
    * Determines whether the parameter list is completely supplied
    *
    * @param params
    * @return
    */
  private def isComplete(params: Seq[(String, (Runscript.Evaluation, Option[Argument]))]) : Boolean  = {

    // If we have an argument for all parameters, we are done
    params.forall( item  => item._2._2.isDefined)
  }



  // Set of sessionIDs of all users that are subscribed to this Job
  startWith(Unemployed, Empty)

  when(Unemployed) {

    case Event(CreateJob(jobID, RunscriptData(toolname, params)), Empty) =>

      // Change to current JobID
      this.currentJobID = Some(jobID)
      this.executionContext = Some(ExecutionContext(jobPath/jobID))

      // Representation of the current State of the job submission
      var parameters : Seq[(String, (Evaluation, Option[Argument]))] = runscriptManager(toolname).parameters.map { t =>
        t._1 -> (t._2 -> None)
      }
      for((paramName, value) <- params) {

        parameters  = supply(paramName, value, parameters)
      }

      Logger.info("JobActor has supplied all parameters and now decides what to do")
      Logger.info(isComplete(parameters).toString)


      stay using Empty
  }


  initialize()
}



/*
    class Buncher extends FSM[State, Data] {

      startWith(Idle, Uninitialized)

      when(Idle) {
        case Event(SetTarget(ref), Uninitialized) =>
          stay using Todo(ref, Vector.empty)
      }

      // transition elided ...

      when(Active, stateTimeout = 1 second) {
        case Event(Flush | StateTimeout, t: Todo) =>
          goto(Idle) using t.copy(queue = Vector.empty)
      }

      // unhandled elided ...

      initialize()
    }



 */