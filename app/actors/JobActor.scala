package actors

import java.io.{FileOutputStream, ObjectOutputStream}
import javax.inject.Inject

import actors.JobActor._
import akka.actor.{Actor, ActorRef}
import akka.event.LoggingReceive
import models.Constants
import models.database.jobs._
import models.database.statistics.{JobEvent, JobEventLog}
import models.database.users.User
import models.search.JobDAO
import modules.tel.runscripts._
import better.files._
import com.typesafe.config.ConfigFactory
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

  // UserActor Logged in and needs to change the user IDs
  case class SwapUserID(oldUserID : BSONObjectID, newUserID : BSONObjectID)

  // JobActor is requested to Delete the job
  case class Delete(jobID: String)

  // Job Controller receives a job state change from the SGE
  case class JobStateChanged(jobID : String, jobState : JobState)
}

class JobActor @Inject() (             runscriptManager        : RunscriptManager, // To get runscripts to be executed
                                       env                     : Env, // To supply the runscripts with an environment
                                   val reactiveMongoApi        : ReactiveMongoApi,
                                   val jobDao                  : JobDAO,
                                       wrapperExecutionFactory : WrapperExecutionFactory,
                          implicit val locationProvider        : LocationProvider,
 @NamedCache("userCache") implicit val userCache               : CacheApi)
  extends Actor
    with Constants
    with UserSessions
    with CommonModule {

  // Attributes asssocidated with a Job
  private var currentJobs: Map[String, Job] = Map.empty
  private var currentJobLogs: Map[String, JobEventLog] = Map.empty
  private var currentExecutionContexts: Map[String, ExecutionContext] = Map.empty

  // Map of all connected users and their Websocket Actors
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
    val job = this.currentJobs(jobID)
    // If the job Appears in the running Execution, terminate it
    this.currentJobs = this.currentJobs.-(jobID)

    // Save Job Event Log to the collection and remove it from the map afterwards
    addJobLog(this.currentJobLogs(job.jobID))
    this.currentJobLogs = this.currentJobLogs.-(job.jobID)

    if(this.runningExecutions.contains(jobID)) {
      this.runningExecutions(jobID).terminate()
      this.runningExecutions = this.runningExecutions.-(jobID)
    }
    this.currentExecutionContexts = this.currentExecutionContexts.-(jobID)
  }


  /**
    * Updates Jobstate in Model, in database, and notifies user watchlist
    */
  private def updateJobState(job : Job): Future[Job] = {
    // Push the updated job into the current jobs
    this.currentJobs = this.currentJobs.updated(job.jobID, job)

    // Update job in the database and notify watcher upon completion
    modifyJob(BSONDocument(Job.JOBID -> job.jobID),
      BSONDocument("$set" -> BSONDocument(Job.STATUS -> job.status))).map { modifiedJob =>
      val jobLog = this.currentJobLogs.get(job.jobID) match {
        case Some(jobEventLog) => jobEventLog.addJobStateEvent(job.status)
        case None              => JobEventLog(job.mainID, List(JobEvent(job.status, Some(DateTime.now))))
      }
      this.currentJobLogs = this.currentJobLogs.updated(job.jobID, jobLog)
      val foundWatchers = users.filter(a => job.watchList.contains(a._1))
      //Logger.info("\n----\nFound Watchers for Job \'" + job.jobID + "\': " + foundWatchers.keys.map(_.stringify).mkString(", "))
      //Logger.info("Job State for \'" + job.jobID + "\' changed to: " + job.status + "\n----\n")
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



      // set memory allocation on the cluster
      val h_vmem = ConfigFactory.load().getString(s"Tools.$toolname.memory")
      val threads = ConfigFactory.load().getInt(s"Tools.$toolname.threads")
      env.configure(s"MEMORY", h_vmem)
      env.configure("THREADS", threads.toString)
      Logger.info(s"$jobID is running with $h_vmem h_vmem")
      Logger.info(s"$jobID is running with $threads threads")


      val jobCreationTime = DateTime.now()

      // jobid will also be available as parameter
      val extendedParams = params + ("jobid" -> jobID)

      val clusterData = JobClusterData("", Some(h_vmem), Some(threads))

      // Make a new JobObject and set the initial values
      // TODO Currently the job always belongs to the submitting user. Change the OwnerID to None for Public Jobs
      val job = Job(mainID      = BSONObjectID.generate(),
                    jobID       = jobID,
                    ownerID     = Some(user.userID),
                    status      = Submitted,
                    tool        = toolname,
                    clusterData = Some(clusterData),
                    label       = params.get("label"),
                    watchList   = List(user.userID),
                    dateCreated = Some(jobCreationTime),
                    dateUpdated = Some(jobCreationTime),
                    dateViewed  = Some(jobCreationTime))
      this.currentJobs = this.currentJobs.updated(jobID, job)

      // Add job to database
      upsertJob(job)
      val paramsWithoutMainID = params - "mainID" - "jobID" // need to hash without mainID and without the jobID

      // get hold of the database in use


      val DBNAME = params match {

        case x if x isDefinedAt "standarddb" => params.getOrElse("standarddb", "")
        case x if x isDefinedAt "hhblitsdb"  => params.getOrElse("hhblitsdb", "")
        case x if x isDefinedAt "hhsuitedb"  => params.getOrElse("hhsuitedb", "")
        case _ => ""

      }


      val STANDARDDB = (env.get("STANDARD") + "/" + params.getOrElse("standarddb","")).toFile
      val HHBLITSDBMTIME = env.get("HHBLITS").toFile.lastModifiedTime.toString
      val HHSUITEDBMTIME = env.get("HHSUITE").toFile.lastModifiedTime.toString


      val jobHash = {
      params match {
        case x if x isDefinedAt "standarddb" => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some(DBNAME),
          dbMtime = Some(STANDARDDB.lastModifiedTime.toString),
          toolname = toolname,
          jobDao.generateToolHash(toolname),
          dateCreated = Some(jobCreationTime),
          jobID = jobID
        )
        case x if x isDefinedAt "hhsuitedb" => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some(DBNAME),
          dbMtime = Some(HHSUITEDBMTIME),
          toolname = toolname,
          jobDao.generateToolHash(toolname),
          dateCreated = Some(jobCreationTime),
          jobID = jobID
        )
        case x if x isDefinedAt "hhblitsdb" => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some(DBNAME),
          dbMtime = Some(HHBLITSDBMTIME),
          toolname = toolname,
          jobDao.generateToolHash(toolname),
          dateCreated = Some(jobCreationTime),
          jobID = jobID
        )
        case _ => JobHash( mainID = job.mainID,
          jobDao.generateHash(paramsWithoutMainID).toString(),
          jobDao.generateRSHash(toolname),
          dbName = Some("none"), // field must exist so that elasticsearch can do a bool query on multiple fields
          dbMtime = Some("1970-01-01T00:00:00Z"), // use unix epoch time
          toolname = toolname,
          jobDao.generateToolHash(toolname),
          dateCreated = Some(jobCreationTime),
          jobID = jobID)
        }
      }
      hashCollection.flatMap(_.insert(jobHash))

      // Add Job to user in database
      //upsertUser(user.copy(jobs = user.jobs.::(job.jobID)))
      modifyUser(BSONDocument(User.IDDB -> user.userID), BSONDocument("$push" -> BSONDocument(User.JOBS -> jobID))).foreach {
        case Some(updatedUser) =>
          updateUserCache(updatedUser)
        case None =>
          Logger.error("New Job Submission: Could not update user cache in Job Actor")
          Logger.error(user.toString)
      }

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

      // Create a log for this job
      this.currentJobLogs = this.currentJobLogs.updated(job.jobID, JobEventLog(job.mainID, List(JobEvent(job.status, Some(DateTime.now)))))

      // Update the statistics
      increaseJobCount(job.tool)

      // Get new runscript instance from the runscript manager
      val runscript = runscriptManager(toolname).withEnvironment(env)
      // Representation of the current State of the job submission
      var parameters : Seq[(String, (Evaluation, Option[Argument]))] = runscript.parameters.map(t => t._1 -> (t._2 -> None))
      for((paramName, value) <- extendedParams) {
        parameters  = supply(jobID, paramName, value, parameters)
      }
      if(isComplete(parameters)) {
        val pendingExecution = wrapperExecutionFactory.getInstance(runscript(parameters.map(t => (t._1, t._2._2.get.asInstanceOf[ValidArgument]))))
        executionContext.accept(pendingExecution)

        this.runningExecutions = this.runningExecutions.updated(jobID, executionContext.executeNext.run())
        env.remove(s"MEMORY")
        env.remove(s"THREADS")

      } else {
        // TODO Implement Me. This specifies what the JobActor should do if not all parameters have been specified
        Logger.info("STAY")
      }


    case Delete(jobID) =>
      this.currentJobs.get(jobID) match {
        case Some(job) =>
          this.removeJob(jobID)
          // Message user clients to remove the job from their watchlist
          this.users.filter (uw => job.watchList.contains (uw._1) ).values.flatten.foreach (_! ClearJob (job.jobID) )
        case None =>
      }

    // User does no longer watch this Job
    case StopWatch(jobID, userID) =>
      Logger.info("User stops watching JobID " + jobID)
      modifyJob(BSONDocument(Job.JOBID -> jobID),
                BSONDocument("$pull"   -> BSONDocument(Job.WATCHLIST -> userID))).map {
        case Some(updatedJob) =>
          this.currentJobs = this.currentJobs.updated(jobID, updatedJob)
        case None =>
      }
      this.users(userID).foreach(_ ! ClearJob(jobID))

      modifyUser(BSONDocument(User.IDDB -> userID),
                 BSONDocument("$pull"   -> BSONDocument(User.JOBS -> jobID))).foreach {
        case Some(updatedUser) =>
          updateUserCache(updatedUser)
        case None =>
          Logger.error("Stop Watching: Could not update user cache in Job Actor")
      }

    // User Starts watching job
    case StartWatch(jobID, userID) =>
      Logger.info("User stops watching JobID " + jobID)
      modifyJob(BSONDocument(Job.JOBID   -> jobID),
                BSONDocument("$addToSet" -> BSONDocument(Job.WATCHLIST -> userID))).map {
        case Some(updatedJob) =>
          this.currentJobs = this.currentJobs.updated(jobID, updatedJob)
        case None =>
      }

      modifyUser(BSONDocument(User.IDDB   -> userID),
                 BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> jobID))).foreach {
        case Some(updatedUser) =>
          updateUserCache(updatedUser)
        case None =>
          Logger.error("Start Watching: Could not update user cache in Job Actor")
      }

    // User registers to the job actor
    case RegisterUser(userID, userActor) =>
      val u: Set[ActorRef]     = this.users(userID)
      this.users               = this.users.updated(userID, u.+(userActor))

    // Useractor unregisters to this actor
    case UnregisterUser(userID, userActor) =>
      val u: Set[ActorRef]     = this.users(userID)
      this.users               = this.users.updated(userID, u.-(userActor))

    // User actor logged in and needs to copy the websocket actors
    case SwapUserID(oldUserID : BSONObjectID, newUserID : BSONObjectID) =>
      val u: Set[ActorRef]     = this.users(oldUserID)
      this.users               = this.users.updated(newUserID, u)

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

            case Queued =>
              this.updateJobState(job)
            case Running => this.updateJobState(job)

            case Done =>

              // Job is no longer running
              Logger.info("Removing execution context")
              this.runningExecutions = this.runningExecutions.-(job.jobID)
              Logger.info("DONE Removing execution context")

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

                // Update the statistics for the failed job
                increaseJobCount(job.tool, failed = true)
              }
            case _ =>
              Logger.info("Job State for \'" + jobID + "\' changed to invalid JobStateChanged State: " + jobState)
          }
        case None =>
          Logger.info("Job not found: " + jobID)
      }
  }
}
