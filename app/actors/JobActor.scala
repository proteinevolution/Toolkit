package actors

import java.io.{FileOutputStream, ObjectOutputStream}
import javax.inject.{Inject, Named}

import actors.JobActor._
import actors.Master.{CreateJob, WorkerDoneWithJob}
import akka.actor.{Actor, ActorRef, FSM}
import models.Constants
import models.database._
import models.search.JobDAO
import modules.tel.runscripts._
import better.files._
import controllers.UserSessions
import modules.LocationProvider
import modules.tel.env.Env
import modules.tel.execution.{EngineExecution, ExecutionContext, LocalExecution, RunnableExecution}
import modules.tel.runscripts.Runscript.Evaluation
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global


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
  case object Nothing extends JobActorData
  case class  DeletableExecution(process: scala.sys.process.Process, delete: Option[scala.sys.process.ProcessBuilder]) extends JobActorData
  case object SomeData extends  JobActorData

  trait Factory {

    def apply : Actor
  }

  // Messages the jobActor accepts from outside
  case class JobStateChanged(jobID: String, newState: JobState)
  case class StopWatch(actorRef: ActorRef)
  case object Delete
}

class JobActor @Inject() (runscriptManager : RunscriptManager,    // To get runscripts to be executed
                          env: Env,                               // To supply the runscripts with an environment
                          val reactiveMongoApi: ReactiveMongoApi,
                          val jobDao : JobDAO,
                          engineExecutionFactory: EngineExecution.Factory,
                          implicit val locationProvider: LocationProvider,
                          @NamedCache("userCache") implicit val userCache : CacheApi,
                          @Named("master") master: ActorRef)
  extends Actor
    with FSM[JobActorState, JobActorData]
    with Constants
    with UserSessions {

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


  // Make the jobActor unemployed and available to the master again
  private def unemploy() = {

    master ! WorkerDoneWithJob(this.currentJob.get.jobID)
    this.currentJob = None
    this.executionContext = None
    this.watchers.clear()
    goto(Unemployed)
  }


  /**
    * Updates Jobstate in Model, in database, and notifies user watchlist
    */
  private def updateJobState(state: JobState): Unit = {

    // Update job in the database
    this.currentJob = Some(this.currentJob.get.copy(status = state))
    upsertJob(this.currentJob.get)

    // Inform watchlist
    watchers.foreach(_ ! JobStateChanged(this.currentJob.get.jobID, state))
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

      // the jobid needs to be added to the parameters
      val extendedParams = params + ("jobid" -> jobID)

      /*
      FileInputStream fis = new FileInputStream("map.ser");
      ObjectInputStream ois = new ObjectInputStream(fis);
      Map anotherMap = (Map) ois.readObject();
    ois.close();
    */

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
      // Add job to database
      upsertJob(this.currentJob.get)

      // Write jobhash into jobhashes collection

      lazy val paramsWithoutMainID = params - "mainID" - "jobID" // need to hash without mainID and without the jobID
      lazy val DB = params.getOrElse("standarddb","").toFile  // get hold of the database in use

      lazy val jobHash = {
        paramsWithoutMainID.get("standarddb") match {
          case None => JobHash( mainID = this.currentJob.get.mainID,
            jobDao.generateHash(toolname, paramsWithoutMainID).toString(),
            dbName = Some("none"), // field must exist so that elasticsearch can do a bool query on multiple fields
            dbMtime = Some("1970-01-01T00:00:00Z") ) // use unix epoch time


          case _ => JobHash( mainID = this.currentJob.get.mainID,
            jobDao.generateHash(toolname, paramsWithoutMainID).toString(),
            dbName = Some(DB.name),
            dbMtime = Some(DB.lastModifiedTime.toString)
          )
        }
      }

      hashCollection.flatMap(_.insert(jobHash))

      // Add Job to user
      modifyUser(BSONDocument(User.IDDB -> userID),
        BSONDocument("$push" -> BSONDocument(User.JOBS -> jobID)))

      this.executionContext = Some(ExecutionContext(jobPath/jobID))

      // Store the extended Parameters in the working directory for faster reloading
      (jobPath/jobID/"sparam").createIfNotExists(asDirectory = false)
      val fos = new FileOutputStream((jobPath/jobID/"sparam").pathAsString)
      val oos = new ObjectOutputStream(fos)
      oos.writeObject(extendedParams)
      oos.close()

      // Clear old watchers and insert job owner
      this.watchers.clear()
      userWithWS match {
        case (_, Some(actorRef)) => this.watchers.add(actorRef)
        case (_, None) => 
      }

      // Fetch the runscript for the job Execution and provide injected environment
      val runscript = runscriptManager(toolname).withEnvironment(env)


      // Representation of the current State of the job submission
      var parameters : Seq[(String, (Evaluation, Option[Argument]))] = runscript.parameters.map { t =>
        t._1 -> (t._2 -> None)
      }
      for((paramName, value) <- extendedParams) {

        parameters  = supply(paramName, value, parameters)
      }


      if(isComplete(parameters)) {

        // Translate the Runscript into an executable version
        val content = runscript(parameters.map(t => (t._1, t._2._2.get.asInstanceOf[ValidArgument])))

        // Decide on the type of execution based on the context variable. It will
        // either be a local execution or a engine execution
        executionContext.get.accept {

          env.get("CONTEXT") match {

            case "LOCAL" =>
              LocalExecution(content)

            case s: String =>
              this.updateJobState(Queued)
              engineExecutionFactory(content, s)
          }
        }

        // Everyhing is set, start the next execution
        Logger.info("Change state to Employed(Running)")
        val x = executionContext.get.executeNext
        goto(Employed(Running)) using DeletableExecution(x.processbuilder.run(), x.delete)

      } else {

        // TODO Implement Me
        Logger.info("STAY")
        stay using SomeData
      }
  }
  // -----------------------------------------------------------------

  when(Employed(Running)) {


    // Deletion of running Job requested
    case Event(Delete, DeletableExecution(process, deleteOption)) =>

      Logger.info("JobActor deletes running Job instance")
      process.destroy()
      if(deleteOption.isDefined) {
        deleteOption.get.!
      }
      this.unemploy()



    // No longer notify user when job state changes
    case Event(StopWatch(actorRef), _) =>
      this.watchers.remove(actorRef)

      stay()

    // Job State changed has been received during execution
    case Event(JobStateChanged(jobID, state), jobActorData) =>

        this.updateJobState(state)

        state match {

          case Running => // Nothing to do here


          // Runscript execution was succesful of erroneous
          case Done | Error =>

            // We must do more executions if necessary
            if(this.executionContext.get.hasMoreExecutions) {

              // TODO Implement me

            } else  {

                // Job is done and JobActor can be made unemployed
                this.unemploy()
            }
        }
        stay using jobActorData
  }


  initialize()
}

