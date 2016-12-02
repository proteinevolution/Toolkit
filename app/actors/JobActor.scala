package actors

import actors.JobActor.{Initialized, JobEvent}
import akka.actor.{FSM, Props}
import models.database._


/**
  * Created by lzimmermann on 02.12.16.
  */


object JobActor {

  // Job Events that might occur
  sealed trait JobEvent
  case object Delete extends JobEvent
  case object Initialized extends JobEvent


  def props(jobID : String, ownerUserID: String) = Props(new JobActor(jobID, ownerUserID))
}


class JobActor(val jobID: String, val ownerUserID: String) extends FSM[JobState, JobEvent] {

  // Set of sessionIDs of all users that are subscribed to this Job
  private var subscribers = Set(ownerUserID)
  startWith(Submitted, Initialized)








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