package actors

import javax.inject.Inject

import actors.JobActor._
import akka.actor.{Actor, FSM}
import com.google.inject.assistedinject.Assisted
import models.database._
import modules.tel.TEL
import play.api.Logger

import scala.concurrent.duration._


/**
  * Created by lzimmermann on 02.12.16.
  */


object JobActor {

  // Job Events that might occur
  case object Delete


  // Internal Data of the JobActor to work on
  sealed trait JobData
  case object Empty extends JobData
  case class RunscriptData(toolname: String)


  trait Factory {

    def apply(@Assisted("jobID") jobID: String,
              @Assisted("ownerUserID") ownerUserID: String) : Actor
  }
}


class JobActor @Inject() (tel : TEL,
                          @Assisted("jobID") val jobID: String,
                          @Assisted("ownerUserID") val ownerUserID: String) extends Actor with FSM[JobState, JobData] {

  // Set of sessionIDs of all users that are subscribed to this Job
  private var subscribers = Set(ownerUserID)
  startWith(Submitted, Empty)


  when(Submitted, stateTimeout = 1.second ) {

    case Event(StateTimeout, Empty) =>
      Logger.info("JobActor Timeout in Submitted")
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