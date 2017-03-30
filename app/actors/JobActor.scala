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
import modules.tel.runscripts.{LiteralRepresentation, Representation, _}
import better.files._
import com.typesafe.config.ConfigFactory
import controllers.UserSessions
import models.sge.Qdel
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

  // JobActor is requested to Delete the job
  case class Delete(jobID: String)

  // Job Controller receives a job state change from the SGE
  case class JobStateChanged(jobID : String, jobState : JobState)
}

class JobActor @Inject() (               runscriptManager        : RunscriptManager, // To get runscripts to be executed
                                         env                     : Env, // To supply the runscripts with an environment
                                     val reactiveMongoApi        : ReactiveMongoApi,
                                     val jobDao                  : JobDAO,
                                         qdel                    : Qdel,
                                         wrapperExecutionFactory : WrapperExecutionFactory,
                            implicit val locationProvider        : LocationProvider,
   @NamedCache("userCache") implicit val userCache               : CacheApi,
@NamedCache("wsActorCache") implicit val wsActorCache            : CacheApi)
  extends Actor
    with Constants
    with UserSessions
    with CommonModule {

  // Attributes asssocidated with a Job
  private var currentJobs: Map[String, Job] = Map.empty
  private var currentJobLogs: Map[String, JobEventLog] = Map.empty
  private var currentExecutionContexts: Map[String, ExecutionContext] = Map.empty

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

    // If the job is in the current jobs remove it
    this.currentJobs = this.currentJobs.filter(_._1 == jobID)

    // Save Job Event Log to the collection and remove it from the map afterwards
    if(this.currentJobLogs.contains(jobID)) {
      addJobLog(this.currentJobLogs(jobID))
      this.currentJobLogs = this.currentJobLogs.filter(_._1 == jobID)
    }

    // If the job Appears in the running Execution, terminate it
    if(this.runningExecutions.contains(jobID)) {
      this.runningExecutions(jobID).terminate()
      this.runningExecutions = this.runningExecutions.filter(_._1 == jobID)
    }
    this.currentExecutionContexts = this.currentExecutionContexts.filter(_._1 == jobID)
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
        case None              => JobEventLog(mainID      = job.mainID,
                                              toolName    = job.tool,
                                              internalJob = false,
                                              events      = List(JobEvent(job.status, Some(DateTime.now))))
      }
      this.currentJobLogs = this.currentJobLogs.updated(job.jobID, jobLog)
      val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify) : Option[List[ActorRef]])
      foundWatchers.flatten.foreach(_ ! PushJob(job))
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
      var extendedParams = params + ("jobid" -> jobID)

      val clusterData = JobClusterData("", Some(h_vmem), Some(threads))

      // Make a new JobObject and set the initial values
      // TODO Currently the job always belongs to the submitting user. Change the OwnerID to None for Public Jobs
      val job = Job(mainID      = BSONObjectID.generate(),
                    jobID       = jobID,
                    ownerID     = Some(user.userID),
                    //project     = Some(BSONObjectID.generate()),
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
      modifyUserWithCache(BSONDocument(User.IDDB -> user.userID), BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> jobID)))

      // Establish exection context for the newJob
      val executionContext = ExecutionContext(jobPath/jobID)
      this.currentExecutionContexts = this.currentExecutionContexts.updated(jobID, executionContext)

      // Create a log for this job
      // TODO may want to use a different way to identify our users
      val isFromInstitute = user.getUserData.eMail.exists(_.matches(".+@tuebingen.mpg.de"))
      this.currentJobLogs = this.currentJobLogs.updated(job.jobID,
                                                        JobEventLog(mainID      = job.mainID,
                                                                    toolName    = job.tool,
                                                                    internalJob = isFromInstitute,
                                                                    events      = List(JobEvent(job.status, Some(DateTime.now)))))

      // Update the statistics
      increaseJobCount(job.tool)

      // Get new runscript instance from the runscript manager
      val runscript = runscriptManager(toolname).withEnvironment(env)
      // Representation of the current State of the job submission
      var parameters : Seq[(String, (Evaluation, Option[Argument]))] = runscript.parameters.map(t => t._1 -> (t._2 -> Some(ValidArgument(new LiteralRepresentation(new RString("false"))))))
      for((paramName, value) <- extendedParams) {
        parameters  = supply(jobID, paramName, value, parameters)
      }
      // adds the params of the disabled controls from formData, sets value of those to "false"
      for(name <- parameters.map(t => t._1)) {
        if(!(extendedParams contains name)) {
          extendedParams = extendedParams + (name -> "false")
        }
      }

      // Serialize the JobParameters to the JobDirectory
      // Store the extended Parameters in the working directory for faster reloading
      // TODO Use ExecutionContext for file access
      (jobPath/jobID/serializedParam).createIfNotExists(asDirectory = false)
      val oos = new ObjectOutputStream(new FileOutputStream((jobPath/jobID/serializedParam).pathAsString))
      oos.writeObject(extendedParams)
      oos.close()

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
          Logger.info("Removing Job from Elastic Search.")
          jobDao.deleteJob(job.mainID.stringify) // Remove job from elastic search
          Logger.info("Removing Job from current Jobs.")
          this.removeJob(jobID)
          // Message user clients to remove the job from their watchlist
          Logger.info("Informing Users of deletion.")
          val foundWatchers = job.watchList.flatMap(userID => wsActorCache.get(userID.stringify) : Option[List[ActorRef]])
          foundWatchers.flatten.foreach (_ ! ClearJob (job.jobID))
          Logger.info("Deletion Complete.")
        case None =>
      }

      findJobSGE(jobID).map {
        x =>
          Logger.info(s"[CLUSTER] execute qdel " + jobID)
          qdel.delete(x.clusterData.get.sgeID)
      }


    // User does no longer watch this Job
    case StopWatch(jobID, userID) =>
      modifyJob(BSONDocument(Job.JOBID -> jobID),
                BSONDocument("$pull"   -> BSONDocument(Job.WATCHLIST -> userID))).foreach {
        case Some(updatedJob) =>
          this.currentJobs = this.currentJobs.updated(jobID, updatedJob)
        case None =>
      }
      val wsActors = wsActorCache.get(userID.stringify) : Option[List[ActorRef]]
      wsActors.foreach(_.foreach(_ ! ClearJob(jobID)))

      modifyUserWithCache(BSONDocument(User.IDDB -> userID),
                          BSONDocument("$pull"   -> BSONDocument(User.JOBS -> jobID)))

    // User Starts watching job
    case StartWatch(jobID, userID) =>
      modifyJob(BSONDocument(Job.JOBID   -> jobID),
                BSONDocument("$addToSet" -> BSONDocument(Job.WATCHLIST -> userID))).map {
        case Some(updatedJob) =>
          this.currentJobs = this.currentJobs.updated(jobID, updatedJob)
        case None =>
      }

      modifyUserWithCache(BSONDocument(User.IDDB   -> userID),
                          BSONDocument("$addToSet" -> BSONDocument(User.JOBS -> jobID)))

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
