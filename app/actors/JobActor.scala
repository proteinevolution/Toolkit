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

  case class CreateJob(jobID: String,
                       userWithWS: (User, Option[ActorRef]), // TODO Second tuple component is probably not optional
                       toolname: String,
                       params: Map[String, String])
  trait Factory {

    def apply : Actor
  }

  // Messages the jobActor accepts from outside
  case class JobStateChanged(jobID: String, newState: JobState)

  // User Actor starts watching
  case class StartWatch(jobID: String, actorRef: ActorRef)

  // UserActor Stops Watching this Job
  case class StopWatch(jobID: String, actorRef: ActorRef)

  // JobActor is requested to Delete the job
  case class Delete(jobID: String)
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
  private var watchers: Map[String, Set[ActorRef]] = Map.empty.withDefaultValue(Set.empty)

  // Running executions
  private var runningExecutions: Map[String, RunningExecution] = Map.empty


  /** Supplies a value for a particular Parameter. Returns params again if the parameter
    * is not present
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
    this.watchers = this.watchers.-(jobID).withDefaultValue(Set.empty)
  }


  /**
    * Updates Jobstate in Model, in database, and notifies user watchlist
    */
  private def updateJobState(jobID: String, state: JobState): Future[Job] = {

    // Update Job in Map
    val job = this.currentJobs(jobID).copy(status = state)
    this.currentJobs = this.currentJobs.updated(jobID, job)

    // Update job in the database and notify watcher upon completion
    upsertJob(job).map { upsertedJob =>
      watchers(jobID).foreach(_ ! JobStateChanged(upsertedJob.get.jobID, state))
      upsertedJob.get
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

    case CreateJob(jobID, userWithWS, toolname, params) =>

      val jobCreationTime = DateTime.now()
      val userID = userWithWS._1.userID

      // jobid will also be available as parameter
      val extendedParams = params + ("jobid" -> jobID)

      // Make a new JobObject and set the initial values
      val job = Job(mainID = BSONObjectID.generate(),
        parentID    = None,
        jobID       = jobID,
        ownerID     = if (params.getOrElse("private","") == "true") Some(userID) else None,
        status      = Submitted,
        tool        = toolname,
        watchList   = List(userID),
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
    modifyUser(BSONDocument(User.IDDB -> userID), BSONDocument("$push" -> BSONDocument(User.JOBS -> jobID)))

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
    userWithWS match {
      case (_, Some(actorRef)) =>  this.watchers = this.watchers.updated(jobID, Set(actorRef))
      case (_, None) => // TODO Can this happen?
    }

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
    case StopWatch(jobID, actorRef) =>
        val w: Set[ActorRef] = this.watchers(jobID)
        this.watchers = this.watchers.updated(jobID, w.-(actorRef)).withDefaultValue(Set.empty)

    case StartWatch(jobID, actorRef) =>
      Logger.info("User starts watching JobState with JobID " + jobID)
      val w: Set[ActorRef] = this.watchers(jobID)
      this.watchers = this.watchers.updated(jobID, w.+(actorRef)).withDefaultValue(Set.empty)


    // Message from outside that the jobState has changed
    case JobStateChanged(jobID, state) =>

      Logger.info("Jobstate has changed to " + state.toString + " of Job with ID " + jobID)

      // Dependent on the state, we have to do different things
      state match {

        case Queued => this.updateJobState(jobID, state)
        case Running => this.updateJobState(jobID, state)

        case Done =>

          // Job is no longer running
          Logger.info("Removing exection context")
          this.runningExecutions = this.runningExecutions.-(jobID)
          Logger.info("DONE Removing exection context")

          // Put the result files into the database, JobActor has to wait until this process has finished
          Future.sequence((jobPath/jobID/"results").list.withFilter(_.hasExtension).withFilter(_.extension.get == ".json").map { file =>
            result2Job(jobID, file.nameWithoutExtension, Json.parse(file.contentAsString))
          }).map { _ =>
            Logger.info("We can now set the job State")
            // Now we can update the JobState and remove it, once the update has completed
            this.updateJobState(jobID, state).map { job =>
              this.removeJob(job.jobID)
            }
          }
        // Currently no further error handling
        case Error =>
          this.updateJobState(jobID, state).map { job =>
            this.removeJob(job.jobID)
          }
      }
  }
}
