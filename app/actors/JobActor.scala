package actors

import java.io.{FileOutputStream, ObjectOutputStream}
import javax.inject.Inject

import actors.JobActor._
import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import models.Constants
import models.database._
import models.search.JobDAO
import modules.tel.runscripts._
import better.files._
import controllers.UserSessions
import modules.{CommonModule, LocationProvider}
import modules.tel.env.Env
import modules.tel.execution.{ExecutionContext, RunningExecution, WrapperExecutionFactory}
import modules.tel.runscripts.Runscript.Evaluation
import org.joda.time.DateTime
import play.api.Logger
import play.api.cache.{CacheApi, NamedCache}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json._
import scala.concurrent.Future

object JobActor {

  case class CreateJob(jobID     : String,
                       user      : User,
                       toolname  : String,
                       params    : Map[String, String])
  trait Factory {

    def apply : Actor
  }

  // Messages the jobActor accepts from outside
  case class PushJob(job : Job)

  // Message for the Websocket Actor to send a ClearJob Message
  case class ClearJob(jobID : String)

  // User Actor starts watching
  case class StartWatch(jobID: String, userID: BSONObjectID)

  // UserActor Stops Watching this Job
  case class StopWatch(jobID: String, userID: BSONObjectID)

  // UserActor Registers Websocket
  case class RegisterUser(userID : BSONObjectID, userActor : ActorRef)

  // UserActor Unregisters Websocket
  case class UnregisterUser(userID : BSONObjectID, userActor : ActorRef)

  // JobActor is requested to Delete the job
  case class Delete(jobID: String)

  // Job Controller receives a job state change from the SGE
  case class JobStateChanged(jobID : String, jobState : JobState)
}

class JobActor @Inject() (runscriptManager : RunscriptManager, // To get runscripts to be executed
                          env: Env, // To supply the runscripts with an environment
                          val reactiveMongoApi: ReactiveMongoApi,
                          val jobDao : JobDAO,
                          wrapperExecutionFactory: WrapperExecutionFactory,
                          implicit val locationProvider: LocationProvider,
                          @NamedCache("userCache") implicit val userCache : CacheApi)
  extends Actor
    with Constants
    with UserSessions
    with CommonModule {

  // Attributes asssocidated with a Job
  private var currentJobs: Map[String, Job] = Map.empty
  private var currentExecutionContexts: Map[String, ExecutionContext] = Map.empty

  // WebSocketActors which are interested in the state of the Job
  private var watchers: Map[String, Set[BSONObjectID]] = Map.empty.withDefaultValue(Set.empty)

  private var users: Map[BSONObjectID, Set[ActorRef]] = Map.empty.withDefaultValue(Set.empty)

  // Running executions
  private var runningExecutions: Map[String, RunningExecution] = Map.empty


  /** Supplies a value for a particular Parameter. Returns params again if the parameter
    * is not present
    *
    * @param name
    * @param value
    * @param params
    */
  private def supply(jobID: String, name: String, value: String, params: Seq[(String, (Runscript.Evaluation, Option[Argument]))])
  : Seq[(String, (Runscript.Evaluation, Option[Argument]))] = {
      params.map  {
        case (paramName, (evaluation, _)) if paramName == name =>
          val x = Some(evaluation(RString(value), this.currentExecutionContexts(jobID)))
          (name, (evaluation, x))
        case q => q
      }
  }

  // JobActor removes the Job from its maps
  private def removeJob(jobID: String) = {

    // If the job Appears in the running Execution, terminate it
    this.currentJobs = this.currentJobs.-(jobID)
    if(this.runningExecutions.contains(jobID)) {
      this.runningExecutions(jobID).terminate()
      this.runningExecutions = this.runningExecutions.-(jobID)
    }
    this.currentExecutionContexts = this.currentExecutionContexts.-(jobID)

    // TODO Maybe send message to all watchers about JobDeletion
    this.watchers = this.watchers.-(jobID)
  }


  /**
    * Updates Jobstate in Model, in database, and notifies user watchlist
    */
  private def updateJobState(job : Job): Future[Job] = {
    // Push the updated job into the current jobs
    this.currentJobs = this.currentJobs.updated(job.jobID, job)

    // Update job in the database and notify watcher upon completion
    upsertJob(job).map { upsertedJob =>
      val foundWatchers = users.filter(a => watchers(job.jobID).contains(a._1))
      Logger.info("\n----\nFound Watchers for Job \'" + job.jobID + "\': " + foundWatchers.keys.map(_.stringify).mkString(", "))
      Logger.info("Job State for \'" + job.jobID + "\' changed to: " + job.status + "\n----\n")
      foundWatchers.values.flatten.foreach(_ ! PushJob(job))
      //watchers(jobID)
      job
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


  override def receive = LoggingReceive {

    case CreateJob(jobID, user, toolname, params) =>

      val jobCreationTime = DateTime.now()

      // jobid will also be available as parameter
      val extendedParams = params + ("jobid" -> jobID)

      // Make a new JobObject and set the initial values
      // TODO Currently the job always belongs to the submitting user. Change the OwnerID to None for Public Jobs
      val job = Job(mainID      = BSONObjectID.generate(),
                    jobID       = jobID,
                    ownerID     = Some(user.userID),
                    status      = Submitted,
                    tool        = toolname,
                    watchList   = List(user.userID),
                    dateCreated = Some(jobCreationTime),
                    dateUpdated = Some(jobCreationTime),
                    dateViewed  = Some(jobCreationTime))
      this.currentJobs = this.currentJobs.updated(jobID, job)

      // Add job to database
      upsertJob(job)
      val paramsWithoutMainID = params - "mainID" - "jobID" // need to hash without mainID and without the jobID

      // TODO There are more databases than the standarddb
      val DB = params.getOrElse("standarddb","").toFile  // get hold of the database in use

      val jobHash = {
      paramsWithoutMainID.get("standarddb") match {
        case None => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some("none"), // field must exist so that elasticsearch can do a bool query on multiple fields
          dbMtime = Some("1970-01-01T00:00:00Z"), // use unix epoch time
          toolname = toolname,
          jobDao.generateToolHash(toolname))
        case _ => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some(DB.name),
          dbMtime = Some(DB.lastModifiedTime.toString),
          toolname = toolname,
          jobDao.generateToolHash(toolname)
        )
      }
    }
    hashCollection.flatMap(_.insert(jobHash))
    // Add Job to user in database
    modifyUser(BSONDocument(User.IDDB -> user.userID), BSONDocument("$push" -> BSONDocument(User.JOBS -> jobID)))

    // Establish exection context for the newJob
    val executionContext = ExecutionContext(jobPath/jobID)
    this.currentExecutionContexts = this.currentExecutionContexts.updated(jobID, executionContext)

    // Serialize the JobParameters to the JobDirectory
    // Store the extended Parameters in the working directory for faster reloading
    // TODO Use ExecutionContext for file access
    (jobPath/jobID/serializedParam).createIfNotExists(asDirectory = false)
    val oos = new ObjectOutputStream(new FileOutputStream((jobPath/jobID/serializedParam).pathAsString))
    oos.writeObject(extendedParams)
    oos.close()

    // Add user as watcher
    this.watchers = this.watchers.updated(jobID, Set(user.userID))

    // Get new runscript instance from the runscript manager
    val runscript = runscriptManager(toolname).withEnvironment(env)
    // Representation of the current State of the job submission
    var parameters : Seq[(String, (Evaluation, Option[Argument]))] = runscript.parameters.map { t =>
       t._1 -> (t._2 -> None)
    }
    for((paramName, value) <- extendedParams) {

        parameters  = supply(jobID, paramName, value, parameters)
    }
    if(isComplete(parameters)) {
        val pendingExecution = wrapperExecutionFactory.getInstance(runscript(parameters.map(t => (t._1, t._2._2.get.asInstanceOf[ValidArgument]))))
        executionContext.accept(pendingExecution)

        this.runningExecutions = this.runningExecutions.updated(jobID, executionContext.executeNext.run())

      } else {
        // TODO Implement Me. This specifies what the JobActor should do if not all parameters have been specified
        Logger.info("STAY")
      }


    case Delete(jobID) => this.removeJob(jobID)


    // User does no longer watch this Job
    case StopWatch(jobID, userID) =>
      val w: Set[BSONObjectID] = this.watchers(jobID)
      this.watchers = this.watchers.updated(jobID, w.-(userID)).withDefaultValue(Set.empty)
      this.users(userID).foreach(_ ! ClearJob(jobID))

      modifyUser(BSONDocument(User.IDDB -> userID),
                 BSONDocument("$pull"   -> BSONDocument(User.JOBS -> jobID)))

    case StartWatch(jobID, userID) =>
      Logger.info("User starts watching JobState with JobID " + jobID)
      val w: Set[BSONObjectID] = this.watchers(jobID)
      this.watchers = this.watchers.updated(jobID, w.+(userID)).withDefaultValue(Set.empty)

    case RegisterUser(userID, userActor) =>
      val u: Set[ActorRef]     = this.users(userID)
      this.users    = this.users.updated(userID, u.+(userActor))

    case UnregisterUser(userID, userActor) =>


    // Message from outside that the jobState has changed
    case JobStateChanged(jobID : String, jobState : JobState) =>
      currentJobs.get(jobID) match {
        case Some(oldJob) =>
          // Update the job object
          val job = oldJob.copy(status = jobState)
          // Give a update message to all
          Logger.info("Jobstate has changed to " + job.status.toString + " of Job with ID " + job.jobID)

          // Dependent on the state, we have to do different things
          job.status match {

            case Queued => this.updateJobState(job)
            case Running => this.updateJobState(job)

            case Done =>
              // Job is no longer running
              Logger.info("Removing exection context")
              this.runningExecutions = this.runningExecutions.-(job.jobID)
              Logger.info("DONE Removing exection context")

              // Put the result files into the database, JobActor has to wait until this process has finished
              Future.sequence((jobPath / job.jobID / "results").list.withFilter(_.hasExtension).withFilter(_.extension.get == ".json").map { file =>
                result2Job(job.jobID, file.nameWithoutExtension, Json.parse(file.contentAsString))
              }).map { _ =>
                // Now we can update the JobState and remove it, once the update has completed
                this.updateJobState(job).map { job =>
                  this.removeJob(job.jobID)
                  Logger.info("Job has been removed from JobActor")
                }
              }
            // Currently no further error handling
            case Error =>
              this.updateJobState(job).map { job =>
                this.removeJob(job.jobID)
              }
            case _ =>
              Logger.info("Job State for \'" + jobID + "\' changed to invalid JobStateChanged State: " + jobState)
          }
        case None =>
          Logger.info("Job not found: " + jobID)
      }
  }
}
