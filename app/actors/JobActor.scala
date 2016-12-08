package actors

import javax.inject.{Inject, Named}

import actors.JobActor._
import actors.Master.CreateJob
import akka.actor.{Actor, ActorRef, FSM}
import com.google.inject.assistedinject.Assisted
import models.database._
import modules.tel.TEL
import play.api.Logger

import scala.concurrent.duration._


/**
  * Created by lzimmermann on 02.12.16.
  */


object JobActor {


  // The JobActor is either Idle or represent a Jobstate
  sealed trait JobActorState
  case class Employed(state: JobState) extends JobActorState
  case object Unemployed extends JobActorState


  // Internal Data of the JobActor to work on
  sealed trait JobData
  case object Empty extends JobData
  case class RunscriptData(toolname: String, params: Map[String, String]) extends JobData


  trait Factory {

    def apply : Actor
  }
}

class JobActor @Inject() (tel : TEL,
                          @Named("master") master: ActorRef) extends Actor with FSM[JobActorState, JobData] {

  // Set of sessionIDs of all users that are subscribed to this Job
  startWith(Unemployed, Empty)

  when(Unemployed) {

    case Event(CreateJob(jobID, RunscriptData(toolname, params)), Empty) =>

      Logger.info("Deal with " + toolname)
      Logger.info("Params " + params.mkString)
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