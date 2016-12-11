package actors

import javax.inject.{Inject, Named}

import actors.JobActor._
import actors.Master.CreateJob
import akka.actor.{Actor, ActorRef, FSM}
import models.Constants
import models.database._
import modules.tel.runscripts._
import better.files._
import modules.CommonModule
import modules.tel.runscripts.Runscript.Evaluation
import org.joda.time.DateTime
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONObjectID

import scala.collection.mutable


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

  case class RunscriptData(toolname: String, params: Map[String, String]) extends JobData

  // Data that the JobActor operates on
  sealed trait JobActorData
  case class Parameters(params: Map[String, String]) extends JobActorData
  case object Nothing extends JobActorData

  trait Factory {

    def apply : Actor
  }

  // Messages sent to the Watchers
  case class JobStateChanged(jobID: String, newState: JobState)
}

class JobActor @Inject() (runscriptManager : RunscriptManager,
                          val reactiveMongoApi: ReactiveMongoApi,
                          @Named("master") master: ActorRef)
  extends Actor
    with FSM[JobActorState, JobActorData]
    with Constants
    with CommonModule {

  var currentJob : Option[Job] = None
  var executionContext: Option[ExecutionContext] = None

  // All actors that are currently monitoring this job
  val watchers : mutable.Set[ActorRef] = mutable.Set[ActorRef]()


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
  startWith(Unemployed, Nothing)


  // ------   Unemployed   ---------------------------------------------------
  when(Unemployed) {

    case Event(CreateJob(jobID, userWithWS,  RunscriptData(toolname, params)), Nothing) =>

      // Job Initialization
      val jobCreationTime = DateTime.now()
      val userID = userWithWS._1.userID

      // Make a new JobObject
      this.currentJob = Some(Job(mainID = BSONObjectID.generate(),
                            sgeID       = "",
                            jobType     = "",
                            parentID    = None,
                            jobID       = jobID,
                            ownerID     = if (params.getOrElse("private","") == "true") Some(userID) else None,
                            status      = Submitted,
                            tool        = toolname,
                            statID      = "",
                            watchList   = List(userID),
                            runtime     = Some(""),
                            memory      = Some(0),
                            threads     = Some(0),
                            dateCreated = Some(jobCreationTime),
                            dateUpdated = Some(jobCreationTime),
                            dateViewed  = Some(jobCreationTime)))
      upsertJob(this.currentJob.get)
      //this.executionContext = Some(ExecutionContext(jobPath/jobID))

      this.watchers.clear()
      userWithWS match {
        case (_, Some(actorRef)) => this.watchers.add(actorRef)
      }

      // Representation of the current State of the job submission
      var parameters : Seq[(String, (Evaluation, Option[Argument]))] = runscriptManager(toolname).parameters.map { t =>
        t._1 -> (t._2 -> None)
      }
      for((paramName, value) <- params) {

        parameters  = supply(paramName, value, parameters)
      }


      if(isComplete(parameters)) {

        // TODO We currently have the assumption that all Arguments are valid
        val arguments = parameters.map(t => (t._1, t._2._2.get.asInstanceOf[ValidArgument]))

        // Write runscript file





        goto(Employed(Running)) using Parameters(params)

      } else {

        // TODO Implement Me
        stay using Nothing
      }
  }
  // -----------------------------------------------------------------

  when(Employed(Running)) {


    case Event(_,_) =>

        Logger.info("Now working")
        stay using Nothing
  }


  onTransition {

    // If we change to a new Employed state, notify all watchers
    case _ -> Employed(Running) =>





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